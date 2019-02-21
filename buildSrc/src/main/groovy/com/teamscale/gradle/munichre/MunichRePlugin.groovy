package com.teamscale.gradle.munichre

import com.teamscale.gradle.PluginUtils
import org.gradle.api.Project

class MunichRePlugin {
	private static final String DASHBOARD_TASKS = "MunichRe Dashboard Utils"
	private static final String HELPER_TASKS = "MunichRe Helper"

	static void create(Project project) {
		project.extensions.create(DashboardExtension.NAME, DashboardExtension)
		PluginUtils.createTask(project, CreateUniformDashboards, DASHBOARD_TASKS)
		PluginUtils.createTask(project, MuteTestSuccessMetric, HELPER_TASKS)
	}
}
