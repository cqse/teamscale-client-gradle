package com.teamscale.gradle.azureDevOps.tasks


import com.teamscale.gradle.azureDevOps.data.Build
import com.teamscale.gradle.azureDevOps.data.Definition
import com.teamscale.gradle.azureDevOps.utils.BuildUtils
import com.teamscale.gradle.teamscale.TeamscaleClient
import com.teamscale.gradle.teamscale.TeamscaleExtension

import static EBuildInformationType.TEST_RESULT
import static com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils.log
import static com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils.warn

class UploadTestResultsTask extends UploadTask {
	final static String NAME = "uploadTestResults"

	@Override
	boolean isConfiguredForTask(Definition definition) {
		return (definition.options.tests != null && definition.options.tests.resultOptions)
	}

	@Override
	String getRejectReason() {
		return "No test results configured"
	}

	@Override
	EBuildInformationType getUploadType() {
		return TEST_RESULT
	}

	@Override
	void run(Definition definition, Build build) {
		def options = definition.options.tests.resultOptions

		// get test result files
		List<File> testResults
		if (options.mustSearchInArtifact()) {
			testResults = BuildUtils.getFilesFromBuildArtifact(definition, build, options)
		} else {
			testResults = BuildUtils.getFilesFromTestRuns(definition, build, options)
		}

		if (testResults.isEmpty()) {
			log("No test results found. The options '$options)' didn't match anything. Nothing uploaded", definition, build)
			definition.setLastProcessedTime(getUploadType(), build)
			return
		}

		// upload to teamscale
		TeamscaleClient http = TeamscaleExtension.getFrom(project).http

		def standard = getStandardQueryParameters(EUploadPartitionType.TEST_RESULTS, definition, build)
		def type = options.type.toString()
		def contents = testResults.collect { it.text }

		def optional = [:]
		if (definition.options.partition) {
			optional = ["path-prefix": definition.options.partition]
		}

		String result = http.uploadExternalReports(standard, contents, type, optional)

		if (result == TeamscaleClient.UPLOAD_SUCCESS_RETURN) {
			log("$type (${contents.size()}): $result", definition, build)
			definition.setLastProcessedTime(getUploadType(), build)
		} else {
			warn("Upload was not successful: $result", definition, build)
		}
	}
}