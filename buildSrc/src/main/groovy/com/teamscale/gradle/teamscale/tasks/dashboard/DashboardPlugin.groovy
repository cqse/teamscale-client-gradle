package com.teamscale.gradle.teamscale.tasks.dashboard

import org.gradle.api.Project

import static com.teamscale.gradle.PluginUtils.createTask

class DashboardPlugin {
	public static final String UTIL_TASKS = "Teamscale Dashboard Utils"

	static void createInstanceTasks(Project project) {
		createTask(project, CheckForOrphanedDashboardsTask, UTIL_TASKS)
	}
}
