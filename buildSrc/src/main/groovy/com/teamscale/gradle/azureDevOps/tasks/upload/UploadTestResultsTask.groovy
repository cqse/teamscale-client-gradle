package com.teamscale.gradle.azureDevOps.tasks.upload

import com.teamscale.gradle.azureDevOps.data.Build
import com.teamscale.gradle.azureDevOps.data.Definition
import com.teamscale.gradle.azureDevOps.tasks.EBuildInformationType
import com.teamscale.gradle.azureDevOps.tasks.EUploadPartitionType
import com.teamscale.gradle.azureDevOps.utils.AdosUtils
import com.teamscale.gradle.teamscale.TeamscaleClient
import com.teamscale.gradle.teamscale.TeamscaleExtension

import static com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils.log

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
		return EBuildInformationType.TEST_RESULT
	}

	@Override
	void run(Definition definition, Build build) {
		def options = definition.options.tests.resultOptions

		// get test result files
		List<File> testResults
		if (options.mustSearchInArtifact()) {
			testResults = AdosUtils.getFilesFromBuildArtifact(definition, build, options)
		} else {
			testResults = AdosUtils.getFilesFromTestRuns(definition, build, options)
		}

		if (testResults.isEmpty()) {
			log("No test results found with '$options)'", definition, build)
			setBuildAsProcessed(definition, build)
			return
		}

		// upload to teamscale
		def standard = getStandardQueryParameters(EUploadPartitionType.TEST_RESULTS, definition, build)
		def type = options.type.toString()
		def contents = testResults.collect { it.text }

		def optional = [:]
		if (definition.options.partition) {
			optional = ["path-prefix": definition.options.partition]
		}

		TeamscaleClient http = TeamscaleExtension.getFrom(project).http
		String result = http.uploadExternalReports(standard, contents, type, optional)

		processUploadResult(definition, build, result, "$type (${contents.size()}): $result")
	}
}