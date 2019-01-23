package com.teamscale.gradle.teamscale.tasks.dashboard


import com.teamscale.gradle.teamscale.TeamscaleClient
import com.teamscale.gradle.teamscale.data.TeamscaleExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils.warn
import static com.teamscale.gradle.teamscale.tasks.dashboard.DashboardUtils.getAllDashboards
import static com.teamscale.gradle.teamscale.tasks.dashboard.DashboardUtils.getDashboard

class CheckForOrphanedDashboardsTask extends DefaultTask {

	public static final String TASK_NAME = "checkForOrphanedDashboards"

	@TaskAction
	def action() {
		TeamscaleClient http = TeamscaleExtension.getFrom(project).http

		def projects = http.getAllProjects()

		getAllDashboards(http).each { dashboardName ->
			def dashboard = getDashboard(http, dashboardName)
			def shared = getSharedProject(dashboard)

			if (shared.isEmpty()) {
				warn("No projects shared for the dashboard $dashboard.name")
				return
			}

			if (!projects.containsAll(shared)) {
				warn("Dashboard [$dashboard.name] is not shared with any existing project: $shared")
			}
		}
	}

	/** Get projects which are shared the given dashboard */
	static List<String> getSharedProject(Object dashboard) {
		List entries = dashboard.projectAccessEntries

		if (entries && entries.size() > 0) {
			return entries.userOrGroup
		}
		return []
	}
}
