package com.teamscale.gradle.azureDevOps.tasks

import com.teamscale.gradle.azureDevOps.config.EBuildInformationType
import com.teamscale.gradle.azureDevOps.data.Build
import com.teamscale.gradle.azureDevOps.data.Definition
import com.teamscale.gradle.teamscale.EAssessment
import com.teamscale.gradle.teamscale.NonCodeMetric
import com.teamscale.gradle.teamscale.TeamscaleClient

import static com.teamscale.gradle.azureDevOps.config.EBuildInformationType.BUILD_STATUS
import static com.teamscale.gradle.azureDevOps.tasks.UploadTask.EPartitionType.BUILD
import static com.teamscale.gradle.azureDevOps.utils.Logging.log
import static com.teamscale.gradle.azureDevOps.utils.Logging.warn
import static com.teamscale.gradle.teamscale.EAssessment.GREEN
import static com.teamscale.gradle.teamscale.EAssessment.RED

class UploadBuildStatusTask extends UploadTask {
	final static String NAME = "uploadBuildStatus"

	final static BUILD_RESULT_MAP = [
		"failed"   : [
			"assessment": RED,
			"content"   : "Build is unstable"
		],
		"succeeded": [
			"assessment": GREEN,
			"content"   : "Build is stable"
		]
	]

	@Override
	EBuildInformationType getUploadType() {
		return BUILD_STATUS
	}

	@Override
	void run(Definition definition, Build build) {
		def queryParams = getStandardQueryParameters(BUILD, definition, build)
		queryParams.appendToMessage("build $build.result")
		def nonCodeMetric = getNonCodeMetric(definition, build)

		TeamscaleClient http = project.teamscale.http
		String result = http.uploadBuildStatus(queryParams, [nonCodeMetric])

		if (result == TeamscaleClient.UPLOAD_SUCCESS_RETURN) {
			log(result, definition, build)
			definition.setLastProcessedTime(getUploadType(), build)
		} else {
			warn("Upload was not successful: $result", definition, build)
		}
	}

	/**
	 * Get the non code metric which is uploaded as the current build status.
	 */
	static NonCodeMetric getNonCodeMetric(Definition definition, Build build) {
		def buildResult = BUILD_RESULT_MAP[build.result]
		String path = createPath("Build Stability", definition)
		String content = buildResult.content
		long time = build.getExecutionTime()

		def nonCodeMetric = new NonCodeMetric(path, content, time)
		nonCodeMetric.addAssessment((EAssessment) buildResult.assessment, 1)

		return nonCodeMetric
	}

	@Override
	boolean isConfiguredForTask(Definition definition) {
		// Build Status is always uploaded
		return true
	}
}
