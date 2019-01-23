package com.teamscale.gradle.teamscale.tasks.dashboard

import com.teamscale.gradle.TeamscalePlugin
import org.gradle.api.Project

class DashboardPlugin {
	public static final String DASHBOARD_TASKS = "Teamscale Dashboard Utils"

	static void createTasks(Project project) {
		TeamscalePlugin.createTask(project, CheckForOrphanedDashboardsTask, DASHBOARD_TASKS)
	}
}
