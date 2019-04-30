package com.teamscale.gradle.munichre

import com.teamscale.gradle.PluginUtils
import com.teamscale.gradle.munichre.extensions.DashboardExtension
import com.teamscale.gradle.munichre.extensions.MonthlyExtension
import com.teamscale.gradle.munichre.tasks.CreateMonthlyIssueQuery
import com.teamscale.gradle.munichre.tasks.CreateUniformDashboards
import org.gradle.api.Project

class MunichRePlugin {
	private static final String DASHBOARD_TASKS = "MunichRe Dashboard Utils"
	private static final String MONTHLY_TASKS = "MunichRe Monthly Utils"

	static  void createProjectTasks(Project project) {
		//
	}

	static void createInstanceTasks(Project project) {
		PluginUtils.createTask(project, CreateUniformDashboards, DASHBOARD_TASKS)
		PluginUtils.createTask(project, CreateMonthlyIssueQuery, MONTHLY_TASKS)
	}

	static void createExtensions(Project project) {
		project.extensions.create(DashboardExtension.NAME, DashboardExtension)
		project.extensions.create(MonthlyExtension.NAME, MonthlyExtension)
	}
}
