package com.teamscale.gradle.teamscale.tasks.util

import com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils
import com.teamscale.gradle.teamscale.TeamscaleClient
import com.teamscale.gradle.teamscale.data.StandardQueryParameter
import com.teamscale.gradle.teamscale.data.TeamscaleExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Writes an empty commit on top of the given partition and is therefore effectively removing the data from
 * that partition.
 */
class MutePartition extends DefaultTask {

	public static final String TASK_NAME = "mutePartition"

	@TaskAction
	def run() {
		TeamscaleExtension teamscale = TeamscaleExtension.getFrom(project)
		TeamscaleClient http = teamscale.http
		assert http: "A teamscale server needs to be configured in order for this to work"

		teamscale.utils.mute.each {
			def partition = it.partition
			def message = it.message

			LoggingUtils.log("Muting '$partition: $message'")

			def standard = new StandardQueryParameter(partition: partition, message: message, t: System.currentTimeMillis().toString())
			http.uploadNonCodeMetrics(standard, [])
		}
	}
}