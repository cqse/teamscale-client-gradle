package com.teamscale.gradle.munichre

import com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils
import com.teamscale.gradle.teamscale.TeamscaleClient
import com.teamscale.gradle.teamscale.data.TeamscaleExtension
import com.teamscale.gradle.teamscale.tasks.dashboard.DashboardUtils
import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class CreateUniformDashboards extends DefaultTask {

	public static final String TASK_NAME = "createUniformDashboards"

	@TaskAction
	def action() {
		TeamscaleClient http = TeamscaleExtension.getFrom(project).http
		DashboardExtension info = project.extensions.getByName(DashboardExtension.NAME) as DashboardExtension

		if (!checkConfig()) {
			return
		}

		def template = DashboardUtils.getDashboard(http, info.template)
		def data = new JsonSlurper().parse(new File(info.propertiesPath))


	}

	boolean checkConfig() {
		DashboardExtension info = project.extensions.getByName(DashboardExtension.NAME) as DashboardExtension
		if (!info) {
			LoggingUtils.log("No dashboard info configured for this task")
			return false
		}

		TeamscaleExtension teamscale = TeamscaleExtension.getFrom(project)
		if (!teamscale?.config?.url) {
			LoggingUtils.warn("No server configured!")
			return false
		}

		if (!info.template) {
			LoggingUtils.warn("No template configured")
			return false
		}
		// throws HTTP Exception; if it does there is no need to continue
		DashboardUtils.getDashboard(teamscale.http, info.template)


		if (!info.propertiesPath) {
			LoggingUtils.warn("No path to the properties configured")
			return false
		}

		if (!new File(info.propertiesPath).exists()) {
			LoggingUtils.warn(String.format("The file {} does not exists", info.propertiesPath))
			return false
		}

		return true
	}
}