package com.teamscale.gradle.azureDevOps.utils

import com.teamscale.gradle.azureDevOps.client.AzureDevOpsClient
import com.teamscale.gradle.azureDevOps.data.AdosBuild
import com.teamscale.gradle.azureDevOps.data.AdosDefinition

import static com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils.warn

class AdosUtils {
	/**
	 * Downloads the all files matching the artifact- and file-pattern in the given options.
	 * The artifactPattern in the given options must be set!
	 */
	static List<File> getFilesFromBuildArtifact(AdosDefinition definition, AdosBuild build, ReportLocationMatcher options) {
		List<File> files = new ArrayList<>()

		if (!options.mustSearchInArtifact()) {
			warn("No artifact pattern given: ${options.toString()}", definition, build)
			return files
		}
		List<Object> artifacts = definition.http.getArtifacts(build.id).findAll { artifact ->
			options.artifactMatches((String) artifact.name)
		}

		if (artifacts.size() == 0) {
			warn("No artifacts found with the given pattern: ${options.toString()}", definition, build)
			return files
		}

		artifacts.each { artifact ->
			def contents = definition.http.getArtifactContents(artifact)

			if (!contents) {
				warn("The contents for the artifact [$artifact.name] could not be found.\n" +
					"Probably a different `data` field. It should be in the form of `#/<number>/<artifact>`\n" +
					"$artifact", definition, build)
				return
			}

			contents.each { item ->
				if (item.itemType == "file" && options.pathMatches((String) item.path)) {
					files.addAll(definition.http.downloadFiles([item.contentLocation]))
				}
			}
		}

		if (files.size() == 0) {
			warn("No files with the given pattern found in the artifacts: ${options.toString()}")
		}

		return files
	}


	/**
	 * Downloads the files defined by the given options from the attachments of the test runs of the build.
	 */
	static List<File> getFilesFromTestRuns(AdosDefinition definition, AdosBuild build, ReportLocationMatcher options) {
		// get test runs
		List<Integer> testRunsIds = definition.http.getTestRunsForBuild(build.getUri()).findAll {
			it.release == null // Ignore release test runs
		}.id as List<Integer>

		return getFilesFromTestRuns(definition.http, options, testRunsIds)
	}

	/**
	 * Downloads the files defined by the given options from the attachments of the given test runs ids
	 */
	static List<File> getFilesFromTestRuns(AzureDevOpsClient http, ReportLocationMatcher options, List<Integer> testRunsIds) {
		// check if the test runs have attachments
		List<String> attachmentUrls = testRunsIds.collect { http.getAttachmentsOfTestRun(it) }
			.flatten().findAll { attachment ->
			options.pathMatches((String) attachment.fileName)
		}.url as List<String>

		return http.downloadFiles(attachmentUrls)
	}
}
