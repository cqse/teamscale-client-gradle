package com.teamscale.gradle.azureDevOps.tasks.base

import com.teamscale.gradle.azureDevOps.data.IBuild
import com.teamscale.gradle.azureDevOps.data.IDefinition
import com.teamscale.gradle.azureDevOps.tasks.EBuildInformationType
import com.teamscale.gradle.azureDevOps.utils.ReportLocationMatcher
import com.teamscale.gradle.teamscale.TeamscaleClient
import com.teamscale.gradle.teamscale.data.StandardQueryParameter
import com.teamscale.gradle.teamscale.data.TeamscaleExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils.log
import static com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils.warn

/**
 * Superclass for tasks which are based on the processing of a single build
 */
abstract class UploadTask<S extends IDefinition, T extends IBuild> extends DefaultTask {

	/** Base message for the upload */
	final static UPLOAD_MESSAGE = "External Analysis (%s)"

	@TaskAction
	/** Main entry point for the task */
	def action() {
		getDefinitions().each { S definition ->
			if (!isConfiguredForTask(definition)) {
				log(getRejectReason(), definition)
				return
			}

			def noBuildsProcessed = true
			(definition.getBuilds() as List<T>).each { T build ->
				if (hasNotBeenProcessed(definition, build) && canBeProcessed(definition, build)) {
					noBuildsProcessed = false
					run(definition, build)
				}
			}

			if (noBuildsProcessed) {
				log("No builds to process", definition)
			}
		}
	}

	/** Returns all defined definitions */
	abstract List<S> getDefinitions()

	/** The reason a definition has been rejected for this upload task */
	abstract String getRejectReason();

	/** Checks that the build hasn't been processed yet with this task */
	protected boolean hasNotBeenProcessed(S definition, T build) {
		return definition.getCache().hasNotBeenProcessed(definition, build, getUploadType())
	}

	/** Check if the build can be processed by the task. If it is not, then it will be marked as processed */
	protected boolean canBeProcessed(S definition, T build) {
		if (build.hasFailed()) {
			log("Build failed. Skipping.", definition, build)
			setBuildAsProcessed(definition, build)
			return false
		}
		return true
	}

	/** Type of the information which is getting uploaded to teamscale */
	abstract EBuildInformationType getUploadType()

	/** Sets the given build and additional information as processed for the definition */
	protected void setBuildAsProcessed(S definition, T build) {
		definition.getCache().set(definition, getUploadType(), build.getFinishTime())
	}

	/** Run the task */
	abstract void run(S definition, T build)

	/** Check if the preconditions for running the task are fulfilled */
	abstract boolean isConfiguredForTask(S definition)

	/**
	 * Return the default partition part for the upload task.
	 * If the definition has a partition as well it will be appended
	 */
	protected abstract String getDefaultPartition();

	/**
	 * Returns the value for the `t` parameter in every teamscale service call.
	 * The value is determined by the given build. This includes the time when the build was started as well
	 * as the target branch on the teamscale instance.
	 * The start time is taken because an older build might have finished after a newer one and newer results might
	 * than be overwritten in teamscale.
	 */
	static String createRequestTimeParameter(IBuild build) {
		def param = "${build.getStartTime().toEpochMilli()}"
		if (build.getTeamscaleBranch()) {
			param = "${build.getTeamscaleBranch()}:$param"
		}
		return param
	}

	/** Create a path for a non-code-metric in the metrics view based on the partition */
	static String createPath(String basePath, IDefinition definition) {
		return appendPartitionName(basePath, "/", definition)
	}

	/** Create and return the standard parameters for the build. */
	static StandardQueryParameter getStandardQueryParameters(IDefinition definition, IBuild build, String partition) {
		getStandardQueryParameters(build, appendPartitionName(partition, ": ", definition))
	}

	/** Create and return the standard parameters for the build. */
	static StandardQueryParameter getStandardQueryParameters(IDefinition definition, IBuild build,
															 String partition, ReportLocationMatcher options) {
		if (options.partition) {
			return getStandardQueryParameters(build, options.partition)
		}
		return getStandardQueryParameters(definition, build, partition)
	}

	/** Create and return the standard parameters for the build. */
	static StandardQueryParameter getStandardQueryParameters(IBuild build, String partition) {
		def message = String.format(UPLOAD_MESSAGE, partition)
		def t = createRequestTimeParameter(build)
		return new StandardQueryParameter(partition: partition, message: message, t: t)
	}


	/**
	 * Modifies the given partition name by adding any optionally suffixes, which are
	 * defined in the definition object.
	 */
	static String appendPartitionName(String partition, String divider, IDefinition definition) {
		if (definition.getPartition()) {
			return "$partition$divider${definition.getPartition()}"
		}
		return partition
	}

	/**
	 * Checks the result of the teamscale upload. If it successful than the build will be set as processed and
	 * the given message will be logged.
	 */
	protected void processUploadResult(S definition, T build, String result, String successMessage) {
		if (result == TeamscaleClient.UPLOAD_SUCCESS_RETURN) {
			log(successMessage, definition, build)
			setBuildAsProcessed(definition, build)
		} else {
			warn("Upload was not successful: $result")
		}
	}

	/** Returns client which communicates with teamscale */
	TeamscaleClient getTeamscaleClient() {
		return TeamscaleExtension.getFrom(project).http
	}
}