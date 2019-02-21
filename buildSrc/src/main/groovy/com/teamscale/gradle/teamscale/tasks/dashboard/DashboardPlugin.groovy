package com.teamscale.gradle.teamscale.tasks.dashboard

import com.teamscale.gradle.PluginUtils
import org.gradle.api.Project

class DashboardPlugin {
	public static final String DASHBOARD_TASKS = "Teamscale Dashboard Utils"

	static void create(Project project) {
		PluginUtils.createTask(project, CheckForOrphanedDashboardsTask, DASHBOARD_TASKS)
	}
}
