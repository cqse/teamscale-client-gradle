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
	void run(S definition, T build) {
		List<ReportLocationMatcher> configs = getTestResultConfigurations(definition)
		if (configs.size() > 1) {
			Set<String> partitions = new HashSet<>(configs.collect { it.partition })

			assert partitions.size() == configs.size(): "If more than one test result upload is configured for a " +
				"single build, only one is allowed to not have a specific partition. Otherwise the test results from " +
				"the different source will be overwritten as they are uploaded to the same partition "
		}

		for (ReportLocationMatcher config in getTestResultConfigurations(definition)) {
			List<File> files = getResultFiles(definition, build, config)
			upload(definition, build, files, config)
		}
	}

	/** Get the list of coverage configuration */
	abstract List<ReportLocationMatcher> getTestResultConfigurations(S definition)

	/** Fetch the available coverage files for the build with the given configuration */
	abstract List<File> getResultFiles(S definition, T build, ReportLocationMatcher config)

	@Override
	EBuildInformationType getUploadType() {
		return EBuildInformationType.TEST_RESULT
	}

	void upload(S definition, T build, List<File> testResults, ReportLocationMatcher options) {
		try {
			if (testResults.isEmpty()) {
				log("No test results found with \"$options)\"", definition, build)
				setBuildAsProcessed(definition, build)
				return
			}

			// upload to teamscale
			def contents = testResults.collect { it.text }

			def standard = getStandardQueryParameters(definition, build, getDefaultPartition(), options)
			def type = options.type
			standard.appendToMessage(type)

			def optional = [:] as Map

			def partition = options.getPartition()
			if (!partition) {
				partition = definition.getPartition()
			}

			if (partition) {
				optional = ["path-prefix": partition]
			}

			String result = getTeamscaleClient().uploadExternalReports(standard, contents, type, optional)
			processUploadResult(definition, build, result, "$type (${contents.size()}): $result")
		} finally {
			testResults.forEach { Files.deleteIfExists(it.toPath()) }
		}
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
