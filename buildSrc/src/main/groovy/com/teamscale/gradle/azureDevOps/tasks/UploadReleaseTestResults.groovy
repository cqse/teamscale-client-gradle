package com.teamscale.gradle.azureDevOps.tasks

import com.teamscale.gradle.azureDevOps.client.AzureDevOpsClient
import com.teamscale.gradle.azureDevOps.data.Build
import com.teamscale.gradle.azureDevOps.data.Definition
import com.teamscale.gradle.azureDevOps.utils.AdosUtils
import com.teamscale.gradle.teamscale.TeamscaleClient
import com.teamscale.gradle.teamscale.TeamscaleExtension

import java.time.Instant

import static com.teamscale.gradle.azureDevOps.tasks.EBuildInformationType.LAST_RELEASE
import static com.teamscale.gradle.azureDevOps.tasks.EBuildInformationType.RELEASE_BUILD
import static com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils.log
import static com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils.warn

class UploadReleaseTestResults extends UploadTask {
	static final String NAME = "uploadReleaseTestResults"

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


		// get release test runs
		def releases = azureClient.getTestRunsForBuild(build.getUri()).findAll { run ->
			run.release != null
		}.groupBy { run ->
			run.release.id
		}

		releases.entrySet().each {
			def testRuns = it.getValue()
			Instant latestCompleteDate = definition.cache.get(definition, LAST_RELEASE)

			// Check that all test runs are completed and have not been processed yet
			if (allTestRunsCompleted(testRuns) && Instant.parse(testRuns.completedDate.min()) > latestCompleteDate) {

				// get the files from the test runs
				def options = definition.options.tests.releaseResultOptions
				def testResults = AdosUtils.getFilesFromTestRuns(definition, build, options, testRuns.id as List)

				// mark the build as processed if no files can be found
				if (testResults.isEmpty()) {
					setBuildAsProcessed(definition, build)
					setReleaseTestsAsProcessed(definition, latestCompleteDate)
					return
				}

				// get parameters
				def standard = getStandardQueryParameters(EUploadPartitionType.RELEASE_TEST_RESULTS, definition, build)
				def type = options.type.toString()
				def contents = testResults.collect { it.text }

				def params = [:]
				def pathPrefix = ""
				if (definition.options.partition) {
					pathPrefix = "${definition.options.partition} "
				}
				pathPrefix += "Release"
				params["path-prefix"] = pathPrefix

				// upload file
				TeamscaleClient teamscaleClient = TeamscaleExtension.getFrom(project).http
				String result = teamscaleClient.uploadExternalReports(standard, contents, type, params)

				// check result
				if (result == TeamscaleClient.UPLOAD_SUCCESS_RETURN) {
					log("$type (${contents.size()}): $result", definition, build)
					setBuildAsProcessed(definition, build)
					setReleaseTestsAsProcessed(definition, Instant.parse((String) testRuns.completedDate.max()))
				} else {
					warn("Upload was not successful: $result", definition, build)
				}
			}
		}
	}

	protected static boolean allTestRunsCompleted(List testRuns) {
		def states = testRuns.state as Set
		return states.size() == 1 && states[0] == "Completed"
	}

	@Override
	protected void setBuildAsProcessed(Definition definition, Build build) {
		definition.cache.set(definition, getUploadType(), build.finishTime.minusMillis(1))
	}

	protected void setReleaseTestsAsProcessed(Definition definition, Instant time) {
		definition.cache.set(definition, LAST_RELEASE, time)
	}

	@Override
	protected boolean isConfiguredForTask(Definition definition) {
		return definition.options.tests && definition.options.tests.releaseResultOptions
	}
}
