package com.teamscale.gradle.azureDevOps.tasks.base

import com.teamscale.gradle.azureDevOps.data.IBuild
import com.teamscale.gradle.azureDevOps.data.IDefinition
import com.teamscale.gradle.azureDevOps.tasks.EBuildInformationType
import com.teamscale.gradle.azureDevOps.utils.AzureBuildException
import com.teamscale.gradle.azureDevOps.utils.ReportLocationMatcher
import com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils
import com.teamscale.gradle.teamscale.data.EAssessment
import com.teamscale.gradle.teamscale.data.NonCodeMetric

import static com.teamscale.gradle.azureDevOps.tasks.base.EBuildResult.*
import static com.teamscale.gradle.teamscale.data.EAssessment.*

/**
 * Base class for uploading the build status of a build from Azure DevOps Services to teamscale.
 */
abstract class UploadBuildStatusTask<S extends IDefinition, T extends IBuild> extends UploadTask<S, T> {
	public static final String NON_CODE_METRIC_PATH = "Build Stability"
	public static final String PARTITION = "Build"
	public static final String REJECT_REASON = "Parsing of build status not configured"

	/** Upload the build status to teamscale */
	void upload(S definition, T build, ReportLocationMatcher options = null) {
		def queryParams = getStandardQueryParameters(definition, build, getDefaultPartition(), options)
		def nonCodeMetric = getNonCodeMetric(definition, build)
		queryParams.appendToMessage(nonCodeMetric.content)

		String result = getTeamscaleClient().uploadNonCodeMetrics(queryParams, [nonCodeMetric])

		processUploadResult(definition, build, result, result)
	}

	/** Get the non code metric which is uploaded as the current build status. */
	NonCodeMetric getNonCodeMetric(IDefinition definition, IBuild build) {
		def buildResult = getBuildResultInfo(definition, build)
		String path = createPath(NON_CODE_METRIC_PATH, definition)
		String content = buildResult.message
		long time = build.getExecutionTime()

		def nonCodeMetric = new NonCodeMetric(path, content, time)
		nonCodeMetric.addAssessment(buildResult.assessment, 1)

		return nonCodeMetric
	}

	/** Returns information on the result of the given build */
	private BuildResultInfo getBuildResultInfo(IDefinition definition, IBuild build) {
		def url = getBuildUrl(definition, build)
		switch (build.getResult()) {
			case SUCCEEDED:
				return new BuildResultInfo(assessment: GREEN, message: "Build succeeded")
			case PARTIALLY_SUCCEEDED:
				return new BuildResultInfo(assessment: YELLOW, message: "Build partially succeeded: $url")
			case FAILED:
				return new BuildResultInfo(assessment: RED, message: "Build failed: $url")
			default:
				// should not happen. Check the "resultFilter" in CollectNewBuildsTasks
				def message = "Invalid build result: ${build.getResult().value}. Check where the builds are fetched, " +
					"because only builds which failed, succeeded or partially succeeded should be checked."
				throw new AzureBuildException(LoggingUtils.createMessage(message, definition, build))
		}
	}

	protected abstract String getBuildUrl(IDefinition definition, IBuild build);

	/** Data class containing the assessment of a build status and the message for the assessment. */
	private static class BuildResultInfo {
		EAssessment assessment
		String message
	}

	@Override
	protected String getDefaultPartition() {
		return PARTITION
	}

	@Override
	String getRejectReason() {
		return REJECT_REASON
	}

	@Override
	EBuildInformationType getUploadType() {
		return EBuildInformationType.BUILD_STATUS
	}

	@Override
	protected boolean canBeProcessed(S definition, T build) {
		return true
	}
}
