package com.teamscale.gradle.teamscale.tasks.dashboard

import org.gradle.api.Project

import static com.teamscale.gradle.PluginUtils.createTask
import static com.teamscale.gradle.TeamscalePlugin.UTIL_TASKS

class DashboardPlugin {
	static void createTasks(Project project) {
		createTask(project, CheckForOrphanedDashboardsTask, UTIL_TASKS)
	}
}
