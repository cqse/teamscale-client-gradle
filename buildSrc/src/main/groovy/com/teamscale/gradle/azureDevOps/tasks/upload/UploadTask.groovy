package com.teamscale.gradle.azureDevOps.tasks.upload

import com.teamscale.gradle.azureDevOps.config.ReportLocationConfig
import com.teamscale.gradle.azureDevOps.data.Build
import com.teamscale.gradle.azureDevOps.data.Definition
import com.teamscale.gradle.azureDevOps.tasks.EBuildInformationType
import com.teamscale.gradle.teamscale.data.StandardQueryParameter
import com.teamscale.gradle.teamscale.TeamscaleClient
import com.teamscale.gradle.teamscale.data.TeamscaleExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils.log
import static com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils.warn

/**
 * Superclass for tasks which are based on the processing of a single build
 */
abstract class UploadTask extends DefaultTask {

	/** Base message for the upload */
	final static UPLOAD_MESSAGE = "External Analysis (%s)"

	public final static String TASK_GROUP = "Azure Build Information Upload"

	@TaskAction
	def action() {
		TeamscaleExtension.getFrom(project).azureDevOps.definitions.each { Definition definition ->
			if (!isConfiguredForTask(definition)) {
				log(getRejectReason(), definition)
				return
			}

			if (definition.builds.isEmpty()) {
				log("No builds to process", definition)
				return
			}

			def builds = definition.builds.findAll { Build build ->
				hasNotBeenProcessed(definition, build) && canBeProcessed(definition, build)
			}

			builds.each { Build build ->
				run(definition, build)
			}
		}
	}

	/** The reason a definition has been rejected for this upload task */
	abstract String getRejectReason();

	/** Checks that the build hasn't been processed yet with this task */
	protected boolean hasNotBeenProcessed(Definition definition, Build build) {
		return definition.cache.hasNotBeenProcessed(definition, getUploadType(), build)
	}

	/** Check if the build can be processed by the task. If it is not, then it will be marked as processed */
	protected boolean canBeProcessed(Definition definition, Build build) {
		if (build.hasFailed()) {
			log("Build failed. Skipping.", definition, build)
			setBuildAsProcessed(definition, build)
			return false
		}
		return true
	}

	abstract EBuildInformationType getUploadType()

	/** Sets the given build and additional information as processed for the definition */
	protected void setBuildAsProcessed(Definition definition, Build build) {
		definition.setLastProcessedTime(getUploadType(), build.getFinishTime())
	}

	/** Run the task */
	abstract void run(Definition definition, Build build)

	/** Check if the preconditions for running the task are fulfilled */
	protected abstract boolean isConfiguredForTask(Definition definition)

	/**
	 * Return the default partition part for the upload task.
	 * If the definition has a partition as well it will be appended
	 */
	protected abstract String getDefaultPartitionPart();

	/**
	 * Returns the value for the `t` parameter in every teamscale service call.
	 * The value is determined by the given build. This includes the time when the build finished as well
	 * as the target branch on the teamscale instance.
	 */
	static String createRequestTimeParameter(Build build) {
		def param = build.queueTime.toEpochMilli()
		if (build.targetBranch) {
			param = "$build.targetBranch:$param"
		}
		return param
	}

	/** Create a path for a non-code-metric in the metrics view based on the partition */
	static String createPath(String basePath, Definition definition) {
		return appendPartitionName(basePath, "/", definition)
	}

	/**
	 * Create and return the standard parameters for the build.
	 */
	StandardQueryParameter getStandardQueryParameters(Definition definition, Build build) {
		getStandardQueryParameters(build, appendPartitionName(getDefaultPartitionPart(), ":", definition))
	}

	/**
	 * Create and return the standard parameters for the build.
	 */
	StandardQueryParameter getStandardQueryParameters(Definition definition, Build build, ReportLocationConfig options) {
		if (!options.partition) {
			return getStandardQueryParameters(definition, build)
		}
		return getStandardQueryParameters(build, options.partition)
	}

	/**
	 * Create and return the standard parameters for the build.
	 */
	static StandardQueryParameter getStandardQueryParameters(Build build, String partition) {
		def message = String.format(UPLOAD_MESSAGE, partition)
		def t = createRequestTimeParameter(build)
		return new StandardQueryParameter(partition: partition, message: message, t: t)
	}


	/**
	 * Modifies the given partition name by adding any optionally suffixes, which are
	 * defined in the definition object.
	 */
	static String appendPartitionName(String partition, String divider, Definition definition) {
		if (definition.options.partition) {
			return "$partition$divider $definition.options.partition"
		}
		return partition
	}

	/**
	 * Checks the result of the teamscale upload. If it successful than the build will be set as processed and
	 * the given message will be logged.
	 */
	protected void processUploadResult(Definition definition, Build build, String result, String successMessage) {
		if (result == TeamscaleClient.UPLOAD_SUCCESS_RETURN) {
			log(successMessage, definition, build)
			setBuildAsProcessed(definition, build)
		} else {
			warn("Upload was not successful: $result", definition, build)
		}
	}
}