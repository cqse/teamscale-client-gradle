package com.teamscale.gradle.azureDevOps.tasks.upload

import com.teamscale.gradle.azureDevOps.data.AdosBuild
import com.teamscale.gradle.azureDevOps.data.AdosDefinition
import com.teamscale.gradle.azureDevOps.tasks.EBuildInformationType
import com.teamscale.gradle.azureDevOps.tasks.base.AdosUploadTask
import com.teamscale.gradle.azureDevOps.utils.AdosUtils
import com.teamscale.gradle.azureDevOps.utils.ReportLocationMatcher
import com.teamscale.gradle.teamscale.data.TeamscaleExtension

import static com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils.log
import static com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils.warn

class UploadAdosExternalReportsTask extends AdosUploadTask {
	static final String TASK_NAME = "uploadAdosBuildReports"
	public static final String PARTITION = "Metrics"
	public static final String REJECT_REASON = "No build report upload configured"

	@Override
	void run(AdosDefinition definition, AdosBuild build) {
		List<ReportLocationMatcher> reports = definition.options.reports

		def processedReports = []

		try {
			reports.each { options ->
				List<File> files = AdosUtils.getFilesFromBuildArtifact(definition, build, options)

				if (files.isEmpty()) {
					log("No reports found for [$options]", definition, build)
					return
				}

				def standard = getStandardQueryParameters(definition, build, getDefaultPartition(), options)
				def type = options.type.toString()
				def contents = files.text

				standard.appendToPartition(" ($options.type)")

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
	EBuildInformationType getUploadType() {
		return EBuildInformationType.BUILD_REPORTS
	}

	@Override
	String getRejectReason() {
		return REJECT_REASON
	}

	@Override
	protected boolean isConfiguredForTask(AdosDefinition definition) {
		return !definition.options.reports.isEmpty()
	}

	@Override
	protected String getDefaultPartition() {
		return PARTITION
	}
}
