package com.teamscale.gradle.azureDevOps.tasks.upload

import com.teamscale.gradle.azureDevOps.data.Build
import com.teamscale.gradle.azureDevOps.data.Definition
import com.teamscale.gradle.azureDevOps.tasks.EBuildInformationType
import com.teamscale.gradle.azureDevOps.utils.AzureBuildException
import com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils
import com.teamscale.gradle.teamscale.EAssessment
import com.teamscale.gradle.teamscale.NonCodeMetric
import com.teamscale.gradle.teamscale.TeamscaleClient
import com.teamscale.gradle.teamscale.TeamscaleExtension

import static com.teamscale.gradle.azureDevOps.tasks.EUploadPartitionType.BUILD
import static com.teamscale.gradle.teamscale.EAssessment.*

class UploadBuildStatusTask extends UploadTask {
	final static String NAME = "uploadNonCodeMetrics"

	@Override
	EBuildInformationType getUploadType() {
		return EBuildInformationType.BUILD_STATUS
	}

	@Override
	void run(Definition definition, Build build) {
		def queryParams = getStandardQueryParameters(BUILD, definition, build)
		def nonCodeMetric = getNonCodeMetric(definition, build)
		queryParams.appendToMessage(nonCodeMetric.content)

		TeamscaleClient http = TeamscaleExtension.getFrom(project).http
		String result = http.uploadNonCodeMetrics(queryParams, [nonCodeMetric])

		processUploadResult(definition, build, result, result)
	}

	/** Get the non code metric which is uploaded as the current build status. */
	static NonCodeMetric getNonCodeMetric(Definition definition, Build build) {
		def buildResult = getBuildResultInfo(definition, build)
		String path = createPath("Build Stability", definition)
		String content = buildResult.message
		long time = build.getExecutionTime()

		def nonCodeMetric = new NonCodeMetric(path, content, time)
		nonCodeMetric.addAssessment(buildResult.assessment, 1)

		return nonCodeMetric
	}

	/** Returns information on the result of the given build */
	static BuildResultInfo getBuildResultInfo(Definition definition, Build build) {
		switch (build.result) {
			case "succeeded":
				return new BuildResultInfo(assessment: GREEN, message: "Build succeeded")
			case "partiallySucceeded":
				return new BuildResultInfo(assessment: YELLOW, message: "Build partially succeeded")
			case "failed":
				return new BuildResultInfo(assessment: RED, message: "Build failed")
			default:
				// should not happen. Check the "resultFilter" in CollectNewBuildsTasks
				def message = "Invalid build result: $build.result"
				throw new AzureBuildException(LoggingUtils.createMessage(message, definition, build))
		}
	}

	@Override
	boolean isConfiguredForTask(Definition definition) {
		// Build Status is always uploaded
		return true
	}

	@Override
	protected boolean canBeProcessed(Definition definition, Build build) {
		return true
	}

	@Override
	String getRejectReason() {
		// never happens
		return null
	}

	private static class BuildResultInfo {
		EAssessment assessment
		String message
	}
}
