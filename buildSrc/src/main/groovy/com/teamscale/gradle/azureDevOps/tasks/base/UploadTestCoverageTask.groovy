package com.teamscale.gradle.azureDevOps.tasks.base

import com.teamscale.gradle.azureDevOps.data.IBuild
import com.teamscale.gradle.azureDevOps.data.IDefinition
import com.teamscale.gradle.azureDevOps.extensions.AzureDevOps
import com.teamscale.gradle.azureDevOps.tasks.EBuildInformationType
import com.teamscale.gradle.azureDevOps.utils.ReportLocationMatcher
import com.teamscale.gradle.azureDevOps.utils.convert.CSharpTestCoverageConverter
import com.teamscale.gradle.teamscale.data.TeamscaleExtension

import static com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils.log

/**
 * Task handling the down- and uploading of the test coverage of the builds of the a configured definition.
 */
abstract class UploadTestCoverageTask<S extends IDefinition, T extends IBuild> extends UploadTask<S, T> {
	public static final String PARTITION = "Test Coverage"
	public static final String REJECT_REASON = "No test coverage configured"

	void upload(S definition, T build, List<File> coverageFiles, ReportLocationMatcher coverageOptions) {
		if (coverageFiles.isEmpty()) {
			log("No test coverage found with '$coverageOptions'", definition, build)
			setBuildAsProcessed(definition, build)
			return
		}

		// transform coverage
		def type = coverageOptions.type
		List<String> contents = convertCoverage(coverageFiles, type)

		// upload to teamscale
		def standard = getStandardQueryParameters(definition, build, getDefaultPartition(), coverageOptions)
		standard.appendToMessage(type)

		def result = getTeamscaleClient().uploadExternalReports(standard, contents, type)
		processUploadResult(definition, build, result, "$type (${coverageFiles.size()}): $result")
	}

	/**
	 * Convert the given files. Some files must be converted to another format in order
	 * to be uploaded to teamscale.
	 */
	List<String> convertCoverage(List<File> coverageFiles, String type) {
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
			"to provide a code coverage exe in ${AzureDevOps.NAME} with 'codeCoverageExe \"<path>\"'. " +
			"VS_COVERAGE needs to be converted before it can be uploaded to Teamscale"

		assert (new File(path)).exists(): "Code coverage exe at path $path does not exists"

		return path
	}

	@Override
	EBuildInformationType getUploadType() {
		return EBuildInformationType.TEST_COVERAGE
	}

	@Override
	protected String getDefaultPartition() {
		return PARTITION
	}

	@Override
	String getRejectReason() {
		return REJECT_REASON
	}
}
