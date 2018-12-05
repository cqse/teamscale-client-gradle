package com.teamscale.gradle.azureDevOps.tasks

import com.teamscale.gradle.azureDevOps.config.AzureDevOps
import com.teamscale.gradle.azureDevOps.config.EBuildInformationType
import com.teamscale.gradle.azureDevOps.data.Build
import com.teamscale.gradle.azureDevOps.data.Definition
import com.teamscale.gradle.azureDevOps.utils.CSharpCoverageConverter
import com.teamscale.gradle.teamscale.TeamscaleClient
import com.teamscale.gradle.teamscale.TeamscaleExtension

import static com.teamscale.gradle.azureDevOps.config.EBuildInformationType.TEST_COVERAGE
import static com.teamscale.gradle.azureDevOps.utils.Logging.log
import static com.teamscale.gradle.azureDevOps.utils.Logging.warn
import static com.teamscale.gradle.teamscale.TeamscaleClient.UPLOAD_SUCCESS_RETURN

class UploadTestCoverageTask extends UploadTask {
	final static String NAME = "UploadTestCoverage"

	@Override
	EBuildInformationType getUploadType() {
		return TEST_COVERAGE
	}

	@Override
	void run(Definition definition, Build build) {
		def tests = definition.options.tests

		// get from VSTS
		List<File> coverageFiles = definition.http.downloadTestCoverage(build.id)
		if (coverageFiles.isEmpty() && tests.coverage.type != null) {
			log("No coverage found", definition, build)
			return
		}

		// transform coverage
		def type = tests.coverage.type.toString()
		List<String> contents = transformCoverage(coverageFiles, type)

		// upload to teamscale
		def params = getStandardQueryParameters(EPartitionType.TEST, definition, build)
		params.appendToMessage(type)

		TeamscaleClient http = project.teamscale.http
		def result = http.uploadExternalReports(params, contents, type)

		if(result == UPLOAD_SUCCESS_RETURN) {
			log("$type : $result", definition, build)
			definition.setLastProcessedTime(getUploadType(), build)
		} else {
			warn("Upload was not successful: $result", definition, build)
		}
	}

	List<String> transformCoverage(List<File> coverageFiles, String type) {
		if(type == "VS_COVERAGE") {
			return CSharpCoverageConverter.convert(coverageFiles, getCoverageExePath())
		}
		return coverageFiles.collect { it.text }
	}

	String getCoverageExePath() {
		TeamscaleExtension teamscale = project.teamscale
		def path = teamscale.azureDevOps.codeCoverageExePath

		assert path != null: "No code coverage exe given! In order to use VS_COVERAGE you need " +
			"to provide a code coverage exe in ${AzureDevOps.NAME}{} with 'codeCoverageExe \"<path>\"'. " +
			"VS_COVERAGE needs to be converted before it can be uploaded to Teamscale"

		return path
	}

	@Override
	boolean isConfiguredForTask(Definition definition) {
		return definition.options.tests && definition.options.tests.coverage
	}
}
