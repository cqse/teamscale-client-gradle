package com.teamscale.gradle.munichre

import com.teamscale.gradle.PluginUtils
import org.gradle.api.Project

class MunichRePlugin {
	private static final String DASHBOARD_TASKS = "MunichRe Dashboard Utils"

	static void createTasks(Project project) {
		PluginUtils.createTask(project, CreateUniformDashboards, DASHBOARD_TASKS)
	}

	static void createExtensions(Project project) {
		project.extensions.create(DashboardExtension.NAME, DashboardExtension)
	}
}
