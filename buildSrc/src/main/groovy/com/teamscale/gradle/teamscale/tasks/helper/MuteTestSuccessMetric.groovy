package com.teamscale.gradle.teamscale.tasks.helper

import com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils
import com.teamscale.gradle.teamscale.data.StandardQueryParameter
import com.teamscale.gradle.teamscale.TeamscaleClient
import com.teamscale.gradle.teamscale.data.TeamscaleExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.util.regex.Pattern

/**
 * For some projects the test results where uploaded as a non-code metric.
 * Now, for projects which do have the actual test results reports uploaded, the old test success metric is obsolete
 * and in the worst case confusing for the clients.
 * This tasks removes this metric from the Metrics view without deleting the old results, which might be needed in a
 * later report, by uploading an empty result for each old partition.
 */
class MuteTestSuccessMetric extends DefaultTask {

	public static final String TASK_NAME = "MuteTestSuccessMetric"

	/** Matches the old partitions */
	public static final Pattern OLD_PARTITIONS = ~/Test(:.*)?/

	@TaskAction
	def run() {
		TeamscaleClient http = TeamscaleExtension.getFrom(project).http

		def partitions = http.getExternalUploads().groupBy {
			it.partition
		}.keySet().findAll({ name ->
			OLD_PARTITIONS.matcher((String) name).matches()
		})

		partitions.each { partition ->
			LoggingUtils.log("Muting the test success metric for $project.name and partition [$partition]")

			def standard = new StandardQueryParameter(partition: partition, message: "Mute",
				t: System.currentTimeMillis().toString())
			http.uploadNonCodeMetrics(standard, [])
		}
	}
}