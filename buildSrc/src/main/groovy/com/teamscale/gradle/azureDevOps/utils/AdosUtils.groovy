package com.teamscale.gradle.azureDevOps.utils

import com.teamscale.gradle.azureDevOps.client.AzureDevOpsClient
import com.teamscale.gradle.azureDevOps.config.ReportLocationConfig
import com.teamscale.gradle.azureDevOps.data.Build
import com.teamscale.gradle.azureDevOps.data.Definition
import com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils

class AdosUtils {
	/**
	 * Downloads the all files matching the artifact- and file-pattern in the given options.
	 * The artifactPattern in the given options must be set!
	 */
	static List<File> getFilesFromBuildArtifact(Definition definition, Build build, ReportLocationConfig options) {
		List<File> coverageFiles = new ArrayList<>()

		if (!options.mustSearchInArtifact()) {
			return coverageFiles
		}
		List<Object> artifacts = definition.http.getArtifacts(build.id).findAll { artifact ->
			options.artifactMatches((String) artifact.name)
		}

		artifacts.each { artifact ->
			def contents = definition.http.getArtifactContents(artifact)

			if (!contents) {
				LoggingUtils.warn("The contents for the artifact [$artifact.name] could not be found.\n" +
					"Probably a different `data` field. It should be in the form of `#/<number>/<artifact>`\n" +
					"$artifact", definition, build)
				return
			}

			contents.each { item ->
				if (item.itemType == "file" && options.pathMatches((String) item.path)) {
					coverageFiles.addAll(definition.http.downloadFiles([item.contentLocation]))
				}
			}
		}

		return coverageFiles
	}


	/**
	 * Downloads the files defined by the given options from the attachments of the test runs of the build.
	 */
	static List<File> getFilesFromTestRuns(Definition definition, Build build, ReportLocationConfig options) {
		// get test runs
		List<Integer> testRunsIds = definition.http.getTestRunsForBuild(build.getUri()).findAll {
			it.release == null // Ignore release test runs
		}.id

		return getFilesFromTestRuns(definition.http, options, testRunsIds)
	}

	/**
	 * Downloads the files defined by the given options from the attachments of the given test runs ids
	 */
	static List<File> getFilesFromTestRuns(AzureDevOpsClient http, ReportLocationConfig options, List<Integer> testRunsIds) {
		// check if the test runs have attachments
		List<String> attachmentUrls = testRunsIds.collect { http.getAttachmentsOfTestRun(it) }
			.flatten().findAll { attachment ->
			options.pathMatches((String) attachment.fileName)
		}.url

		return http.downloadFiles(attachmentUrls)
	}
}
