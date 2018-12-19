package com.teamscale.gradle.azureDevOps.tasks


import com.teamscale.gradle.azureDevOps.data.Build
import com.teamscale.gradle.azureDevOps.data.Definition
import com.teamscale.gradle.teamscale.TeamscaleClient
import com.teamscale.gradle.teamscale.TeamscaleExtension

import static EBuildInformationType.TEST_RESULT
import static com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils.log
import static com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils.warn

class UploadTestResultsTask extends UploadTask {
	final static String NAME = "uploadTestResults"

	@Override
	boolean isConfiguredForTask(Definition definition) {
		return (definition.options.tests != null)
	}

	@Override
	EBuildInformationType getUploadType() {
		return TEST_RESULT
	}

	@Override
	void run(Definition definition, Build build) {
		def tests = definition.options.tests

		// get test runs
		List<Integer> testRunsIds = definition.http.getTestRunsForBuild(build.getUri()).value.findAll {
			it.release == null // Ignore release test runs
		}.id

		// check if the test runs have attachments
		List<String> attachmentUrls = testRunsIds.collect { definition.http.getAttachmentsOfTestRun(it).value }.flatten().findAll {
			tests.isTestResultFile(it.fileName)
		}.url

		if(attachmentUrls.isEmpty()) {
			log("No result found", definition, build)
			return
		}

		List<File> attachments = definition.http.downloadFiles(attachmentUrls)

		// upload to teamscale
		TeamscaleClient http = TeamscaleExtension.getFrom(project).http

		def params = getStandardQueryParameters(EUploadPartitionType.TEST, definition, build)
		def type = tests.result.type.toString()
		def contents = attachments.collect { it.text }

		String result = http.uploadExternalReports(params, contents, type)

		if(result == TeamscaleClient.UPLOAD_SUCCESS_RETURN) {
			log("$result", definition, build)
			definition.setLastProcessedTime(getUploadType(), build)
		} else {
			warn("Upload was not successful: $result", definition, build)
		}
	}
}