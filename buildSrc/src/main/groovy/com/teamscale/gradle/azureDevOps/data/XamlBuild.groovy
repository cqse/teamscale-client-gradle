package com.teamscale.gradle.azureDevOps.data

import com.teamscale.gradle.azureDevOps.tasks.base.EBuildResult
import com.teamscale.gradle.azureDevOps.utils.AzureBuildException
import com.teamscale.gradle.azureDevOps.utils.ReportLocationMatcher
import com.teamscale.gradle.azureDevOps.utils.ZipUtils

import java.nio.file.Files
import java.nio.file.Path
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.regex.Pattern

/**
 * Representing a XAML build, which are given as zip files.
 * Every information about the build must be parsed from files inside of the archive.
 * The status of the build can be determined by checking if an errors.txt file has content.
 */
class XamlBuild implements IBuild {
	String name
	String targetBranch
	Instant time
	EBuildResult result
	Path archive

	private static Pattern buildLogTimestampPattern = ~/Build started (.*)\./
	private static SimpleDateFormat buildLogTimestampFormat = new SimpleDateFormat("MM/dd/yy hh:mm:ss a")

	XamlBuild(XamlDefinition definition, Path archive) {
		this.archive = archive
		this.name = "$archive.fileName".substring(0, "$archive.fileName".length() - 4) // remove .zip from the end
		this.targetBranch = definition.config.teamscaleBranch
		this.time = getBuildTime(definition)
	}

	@Override
	Instant getStartTime() {
		return time
	}

	@Override
	Instant getFinishTime() {
		return time
	}

	@Override
	long getExecutionTime() {
		return 0
	}

	@Override
	String getTeamscaleBranch() {
		return targetBranch
	}

	@Override
	String getName() {
		return name
	}

	@Override
	boolean hasFailed() {
		return result == EBuildResult.FAILED
	}

	void setResult(EBuildResult result) {
		this.result = result
	}

	Instant getBuildTime(XamlDefinition definition) {
		if (definition.config.timestamp) {
			return parseBuildTimeFromBuildLog(definition.config.timestamp)
		}
		return getTimeOfBuildFromName("$archive.fileName", definition.getName())
	}

	/**
	 * Parses the timestamp of when the build started from the build log
	 * This information should be in the first line of the log.
	 */
	Instant parseBuildTimeFromBuildLog(ReportLocationMatcher buildLogMatcher) {
		List<Path> path = ZipUtils.getMatches(archive, buildLogMatcher)
		try {
			if (path.size() != 1) {
				throw new AzureBuildException("No definite match for a build log found with '$buildLogMatcher.pathPattern' " +
					"in $archive.fileName")
			}

			String firstLine = ""
			path.get(0).withReader("utf-16", {
				firstLine = it.readLine()
			})

			def matcher = buildLogTimestampPattern.matcher(firstLine)
			if (matcher.matches()) {
				try {
					return buildLogTimestampFormat.parse(matcher.group(1)).toInstant()
				} catch (ParseException e) {
					throw new AzureBuildException("'${matcher.group(1)}' could not be parsed with '${buildLogTimestampFormat.toPattern()}'", e)
				}
			} else {
				throw new AzureBuildException("No timestamp match found for first line of build log: $firstLine")
			}
		} finally {
			path.forEach { Files.deleteIfExists(it) }
		}
	}

	/**
	 * The name of the archive file is always build as follows: "<name-of-definition>_yyyyMMdd_HH-
	 */
	static Instant getTimeOfBuildFromName(String fileName, String definitionName) {
		String dateString = fileName.replace(definitionName, "")
		dateString = dateString.toLowerCase().replace(".zip", "")

		return Date.parse("_yyyyMMdd_HH-mm-ss", dateString).toInstant()
	}
}
