package com.teamscale.gradle.azureDevOps.tasks

import com.teamscale.gradle.azureDevOps.config.AzureDevOpsExtension
import com.teamscale.gradle.azureDevOps.data.Build
import com.teamscale.gradle.azureDevOps.data.Definition
import com.teamscale.gradle.azureDevOps.utils.BuildUtils
import com.teamscale.gradle.azureDevOps.utils.convert.CSharpTestCoverageConverter
import com.teamscale.gradle.teamscale.TeamscaleClient
import com.teamscale.gradle.teamscale.TeamscaleExtension

import static EBuildInformationType.TEST_COVERAGE
import static com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils.log
import static com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils.warn
import static com.teamscale.gradle.teamscale.TeamscaleClient.UPLOAD_SUCCESS_RETURN

/**
 * Task handling the down- and uploading of the test coverage of the builds of the a configured definition.
 */
class UploadTestCoverageTask extends UploadTask {
	final static String NAME = "uploadTestCoverage"

	@Override
	EBuildInformationType getUploadType() {
		return TEST_COVERAGE
	}

	@Override
	void run(Definition definition, Build build) {
		def coverageOptions = definition.options.tests.coverageOptions

		List<File> coverageFiles
		// The test coverage can be downloaded from the coverage REST service call or from a
		// published artifact
		if(coverageOptions.mustSearchInArtifact()) {
			coverageFiles = BuildUtils.getFilesFromBuildArtifact(definition, build, coverageOptions)
		} else {
			coverageFiles = definition.http.downloadTestCoverage(build.id)
		}

		if (coverageFiles.isEmpty()) {
			log("No coverage found. Pattern didn't match anything and nothing was uploaded", definition, build)
			definition.setLastProcessedTime(getUploadType(), build)
			return
		}

		// transform coverage
		def type = coverageOptions.type.toString()
		List<String> contents = convertCoverage(coverageFiles, type)

		// upload to teamscale
		def standard = getStandardQueryParameters(EUploadPartitionType.TEST_COVERAGE, definition, build)
		standard.appendToMessage(type)

		TeamscaleClient http = TeamscaleExtension.getFrom(project).http
		def result = http.uploadExternalReports(standard, contents, type)

		if(result == UPLOAD_SUCCESS_RETURN) {
			log("$type (${coverageFiles.size()}): $result", definition, build)
			definition.setLastProcessedTime(getUploadType(), build)
		} else {
			warn("Upload was not successful: $result", definition, build)
		}
	}

	/**
	 * Convert the given files. Some files must be converted to another format in order
	 * to be uploaded to teamscale.
	 */
	private List<String> convertCoverage(List<File> coverageFiles, String type) {
		switch (type) {
			case "VS_COVERAGE":
				return CSharpTestCoverageConverter.convert(coverageFiles, getCoverageExePath())
			default:
				return coverageFiles.collect { it.text }
		}
	}

	/**
	 * Get the path to the executable which can convert a .coverage file to
	 * an .xml
	 */
	private String getCoverageExePath() {
		def path = TeamscaleExtension.getFrom(project).azureDevOps.codeCoverageExePath

		assert path != null: "No code coverage exe given! In order to use VS_COVERAGE you need " +
			"to provide a code coverage exe in ${AzureDevOpsExtension.NAME} with 'codeCoverageExe \"<path>\"'. " +
			"VS_COVERAGE needs to be converted before it can be uploaded to Teamscale"

		assert (new File(path)).exists(): "Code coverage exe at path $path does not exists"

		return path
	}

	@Override
	boolean isConfiguredForTask(Definition definition) {
		return definition.options.tests && definition.options.tests.coverageOptions
	}

	@Override
	String getRejectReason() {
		return "No test coverage configured"
	}
}
