package com.teamscale.gradle.azureDevOps.tasks.base

import com.teamscale.gradle.azureDevOps.data.IBuild
import com.teamscale.gradle.azureDevOps.data.IDefinition
import com.teamscale.gradle.azureDevOps.tasks.EBuildInformationType
import com.teamscale.gradle.azureDevOps.utils.ReportLocationMatcher

import java.nio.file.Files

import static com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils.log

abstract class UploadTestResultsTask<S extends IDefinition, T extends IBuild> extends UploadTask<S, T> {
	public static final String PARTITION = "Test Results"
	public static final String REJECT_REASON = "No test results configured"

	@Override
	EBuildInformationType getUploadType() {
		return EBuildInformationType.TEST_RESULT
	}

	void upload(S definition, T build, ReportLocationMatcher options, List<File> testResults) {
		if (testResults.isEmpty()) {
			log("No test results found with '$options)'", definition, build)
			setBuildAsProcessed(definition, build)
			return
		}

		// upload to teamscale
		def contents = []
		try {
			contents = testResults.collect { it.text }
		} finally {
			testResults.forEach { Files.deleteIfExists(it.toPath()) }
		}
		def standard = getStandardQueryParameters(definition, build, getDefaultPartition(), options)
		def type = options.type

		def optional = [:] as Map
		if (definition.getPartition()) {
			optional = ["path-prefix": definition.getPartition()]
		}

		String result = getTeamscaleClient().uploadExternalReports(standard, contents, type, optional)
		processUploadResult(definition, build, result, "$type (${contents.size()}): $result")
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
