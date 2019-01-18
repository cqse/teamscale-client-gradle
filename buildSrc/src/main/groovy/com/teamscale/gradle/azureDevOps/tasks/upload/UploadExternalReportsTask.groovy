package com.teamscale.gradle.azureDevOps.tasks.upload

import com.teamscale.gradle.azureDevOps.config.ReportConfig
import com.teamscale.gradle.azureDevOps.data.Build
import com.teamscale.gradle.azureDevOps.data.Definition
import com.teamscale.gradle.azureDevOps.tasks.EBuildInformationType
import com.teamscale.gradle.azureDevOps.tasks.EUploadPartitionType
import com.teamscale.gradle.azureDevOps.utils.AdosUtils
import com.teamscale.gradle.teamscale.TeamscaleExtension

import static com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils.log
import static com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils.warn

class UploadExternalReportsTask extends UploadTask {
	static final String NAME = "uploadBuildReports"

	@Override
	String getRejectReason() {
		return "No build report upload configured"
	}

	@Override
	EBuildInformationType getUploadType() {
		return EBuildInformationType.BUILD_REPORTS
	}

	@Override
	void run(Definition definition, Build build) {
		List<ReportConfig> reports = definition.options.reports

		def processedReports = []

		try {
			reports.each { options ->
				List<File> files = AdosUtils.getFilesFromBuildArtifact(definition, build, options)

				if (files.isEmpty()) {
					log("No reports found for [$options]", definition, build)
					return
				}

				def standard = getStandardQueryParameters(EUploadPartitionType.BUILD_REPORT, definition, build)
				def type = options.type.toString()
				def contents = files.text

				standard.appendToPartition(options.partition)

				def result = TeamscaleExtension.getFrom(project).http.uploadExternalReports(standard, contents, type)
				processUploadResult(definition, build, result, "$type (${files.size()})")
				processedReports.add(options)
			}
		} catch (Exception e) {
			warn("Processing the build failed. The following reports have already been " +
				"uploaded to teamscale. They may have to be deleted, they will be uploaded again if the build is " +
				"processed again: " + processedReports.toString())
			throw e
		}
	}

	@Override
	protected boolean isConfiguredForTask(Definition definition) {
		return !definition.options.reports.isEmpty()
	}
}
