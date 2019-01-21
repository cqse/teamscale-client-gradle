package com.teamscale.gradle.azureDevOps.tasks.upload

import com.teamscale.gradle.azureDevOps.client.AzureDevOpsClient
import com.teamscale.gradle.azureDevOps.data.Build
import com.teamscale.gradle.azureDevOps.data.Definition
import com.teamscale.gradle.azureDevOps.tasks.EBuildInformationType
import com.teamscale.gradle.azureDevOps.utils.AdosUtils
import com.teamscale.gradle.teamscale.TeamscaleClient
import com.teamscale.gradle.teamscale.TeamscaleExtension

import java.time.Instant

import static com.teamscale.gradle.azureDevOps.tasks.EBuildInformationType.LAST_RELEASE
import static com.teamscale.gradle.azureDevOps.tasks.EBuildInformationType.RELEASE_BUILD
import static com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils.log

class UploadReleaseTestResultsTasks extends UploadTask {
	static final String TASK_NAME = "uploadReleaseTestResults"

	@Override
	String getRejectReason() {
		return "No release tests configured"
	}

	@Override
	EBuildInformationType getUploadType() {
		return RELEASE_BUILD
	}

	@Override
	void run(Definition definition, Build build) {
		AzureDevOpsClient azureClient = definition.http
		build.setLatestProcessedReleaseTest(definition.cache.get(definition, LAST_RELEASE))

		// get release test runs
		def releases = azureClient.getTestRunsForBuild(build.getUri()).findAll { run ->
			run.release != null
		}.groupBy { run ->
			run.release.id
		}

		if (releases.isEmpty()) {
			log("No release found", definition, build)
			return
		}

		releases.entrySet().each {
			def testRuns = it.getValue()

			// Check that all test runs are completed and have not been processed yet
			if (!allTestRunsCompletedAndNotProcessed(testRuns, build.getLatestProcessedReleaseTest())) {
				log("No new releases", definition, build)
				return
			}

			// get the files from the test runs
			def options = definition.options.tests.releaseResultOptions
			List<File> testResults = AdosUtils.getFilesFromTestRuns(definition.http, options, testRuns.id as List)

			// mark the build as processed if no files can be found
			if (testResults.isEmpty()) {
				log("No release tests results found with '$options'", definition, build)
				setBuildAsProcessed(definition, build)
				return
			}

			// Set the processed time. If the results cannot be uploaded this will not be saved
			build.setLatestProcessedReleaseTest(Instant.parse((String) testRuns.completedDate.min()))

			// get parameters
			def standard = getStandardQueryParameters(definition, build, options)
			def type = options.type.toString()
			List<String> contents = testResults.collect { it.text }

			def params = [:]
			def pathPrefix = ""
			if (definition.options.partition) {
				pathPrefix = "${definition.options.partition} "
			}
			pathPrefix += "Release"
			params["path-prefix"] = pathPrefix

			// upload file
			TeamscaleClient http = TeamscaleExtension.getFrom(project).http
			String result = http.uploadExternalReports(standard, contents, type, params)

			// check result
			processUploadResult(definition, build, result, "$type (${contents.size()}): $result")
		}
	}

	/**
	 * Checks whether all given test runs have completed (not in progress) and whether they are more recent than the
	 * provided instant.
	 */
	protected static boolean allTestRunsCompletedAndNotProcessed(List testRuns, Instant lastProcessed) {
		def states = testRuns.state as Set
		def completed = states.size() == 1 && states[0] == "Completed"

		if (completed) {
			return Instant.parse((String) testRuns.completedDate.min()) > lastProcessed
		}
		return false
	}

	@Override
	protected void setBuildAsProcessed(Definition definition, Build build) {
		// Always check the latest build for any new releases
		definition.setLastProcessedTime(getUploadType(), build.getFinishTime().minusMillis(1))
		definition.cache.set(definition, LAST_RELEASE, build.getLatestProcessedReleaseTest())
	}

	@Override
	protected boolean isConfiguredForTask(Definition definition) {
		return definition.options.tests && definition.options.tests.releaseResultOptions
	}

	@Override
	protected String getDefaultPartitionPart() {
		return "Release Test Results"
	}
}
