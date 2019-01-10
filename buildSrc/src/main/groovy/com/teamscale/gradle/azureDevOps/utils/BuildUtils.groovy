package com.teamscale.gradle.azureDevOps.utils

import com.teamscale.gradle.azureDevOps.config.ReportLocationMatcher
import com.teamscale.gradle.azureDevOps.data.Build
import com.teamscale.gradle.azureDevOps.data.Definition
import com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils

import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

import static com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils.log

class BuildUtils {
	/**
	 * Downloads the all files matching the artifact- and file-pattern in the given options.
	 * The artifactPattern in the given options must be set!
	 */
	static List<File> getFilesFromBuildArtifact(Definition definition, Build build, ReportLocationMatcher options) {
		List<File> coverageFiles = new ArrayList<>()

		assert options.mustSearchInArtifact(): "options must have an artifact pattern here! Is probably a missing" +
			"check in the code."

		List<Object> artifacts = definition.http.getArtifacts(build.id).findAll { artifact ->
			options.artifactMatches(artifact.name)
		}

		artifacts.each { artifact ->
			def contents = definition.http.getArtifactContents(artifact)

			if (!contents) {
				LoggingUtils.warn("The contents for the artifact '$artifact.name' could not be found.\n" +
					"Probably a different `data` field. It should be in the form of `#/<number>/<artifact>`\n" +
					"$artifact", definition, build)
				return
			}

			contents.each { item ->
				if (item.itemType == "file" && options.pathMatches(item.path)) {
					coverageFiles.addAll(definition.http.downloadFiles([item.contentLocation]))
				}
			}
		}

		return coverageFiles
	}


	/**
	 * Downloads the files defined by the given options from the attachments of the test runs of the build.
	 */
	static List<File> getFilesFromTestRuns(Definition definition, Build build, ReportLocationMatcher options) {
		// get test runs
		List<Integer> testRunsIds = definition.http.getTestRunsForBuild(build.getUri()).findAll {
			it.release == null // Ignore release test runs
		}.id

		// check if the test runs have attachments
		List<String> attachmentUrls = testRunsIds.collect { definition.http.getAttachmentsOfTestRun(it) }
			.flatten().findAll { attachment ->
			options.pathMatches((String) attachment.fileName)
		}.url

		if (attachmentUrls.isEmpty()) {
			log("No result found", definition, build)
			return []
		}

		return definition.http.downloadFiles(attachmentUrls)
	}

	/**
	 * Tries to convert the given string to a java Pattern object. If the given string cannot be converted
	 * directly because it is a ant-style globbing pattern, it will be converted to a normal regex.
	 *
	 * If it is neither a valid regex or globbing pattern <code>null</code> will be returned.
	 */
	static Pattern getPatternFromRegexOrAntGlobbing(String regexOrGlob) {
		Pattern pattern = getPattern(regexOrGlob)
		if (!pattern) {
			pattern = getPattern(convertGlobToRegex(regexOrGlob))
		}
		return pattern
	}

	private static Pattern getPattern(String regex) {
		try {
			return ~regex
		} catch (PatternSyntaxException e) {
			return null
		}
	}

	/**
	 * Converts a standard POSIX Shell globbing pattern into a regular expression
	 * pattern. The result can be used with the standard {@link java.util.regex} API to
	 * recognize strings which match the glob pattern.
	 * <p/>
	 * See also, the POSIX Shell language:
	 * http://pubs.opengroup.org/onlinepubs/009695399/utilities/xcu_chap02.html#tag_02_13_01
	 *
	 * @param pattern A glob pattern.
	 * @return A regex pattern to recognize the given glob pattern.
	 *
	 * https://stackoverflow.com/questions/1247772/is-there-an-equivalent-of-java-util-regex-for-glob-type-patterns
	 */
	static final String convertGlobToRegex(String pattern) {
		StringBuilder sb = new StringBuilder(pattern.length())
		int inGroup = 0
		int inClass = 0
		int firstIndexInClass = -1
		char[] arr = pattern.toCharArray()
		for (int i = 0; i < arr.length; i++) {
			char ch = arr[i]
			switch (ch) {
				case '\\':
					if (++i >= arr.length) {
						sb.append('\\')
					} else {
						char next = arr[i]
						switch (next) {
							case ',':
								// escape not needed
								break
							case 'Q':
							case 'E':
								// extra escape needed
								sb.append('\\')
							default:
								sb.append('\\')
						}
						sb.append(next)
					}
					break
				case '*':
					if (inClass == 0)
						sb.append(".*")
					else
						sb.append('*')
					break
				case '?':
					if (inClass == 0)
						sb.append('.')
					else
						sb.append('?')
					break
				case '[':
					inClass++
					firstIndexInClass = i + 1
					sb.append('[')
					break
				case ']':
					inClass--
					sb.append(']')
					break
				case '.':
				case '(':
				case ')':
				case '+':
				case '|':
				case '^':
				case '$':
				case '@':
				case '%':
					if (inClass == 0 || (firstIndexInClass == i && ch == '^'))
						sb.append('\\')
					sb.append(ch)
					break
				case '!':
					if (firstIndexInClass == i)
						sb.append('^')
					else
						sb.append('!')
					break
				case '{':
					inGroup++
					sb.append('(')
					break
				case '}':
					inGroup--
					sb.append(')')
					break
				case ',':
					if (inGroup > 0)
						sb.append('|')
					else
						sb.append(',')
					break
				default:
					sb.append(ch)
			}
		}
		return sb.toString()
	}
}
