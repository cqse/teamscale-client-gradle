package com.teamscale.gradle.munichre

import com.teamscale.gradle.PluginUtils
import com.teamscale.gradle.munichre.extensions.DashboardExtension
import com.teamscale.gradle.munichre.extensions.IssueQueryExtension
import com.teamscale.gradle.munichre.tasks.*
import org.gradle.api.Project

class MunichRePlugin {
	private static final String UTILS_TASKS = "MunichRe utils"

	static void createProjectTasks(Project project) {
		//
	}

	static void createInstanceTasks(Project project) {
		PluginUtils.createTask(project, CreateUniformDashboards, UTILS_TASKS)
		PluginUtils.createTask(project, CreateUniformDashboardsOld, UTILS_TASKS)
		PluginUtils.createTask(project, CreateMonthlyIssueQuery, UTILS_TASKS)
		PluginUtils.createTask(project, CreateIssueQueries, UTILS_TASKS)

		PluginUtils.createTask(project, CreateTGAStart, UTILS_TASKS)
		PluginUtils.createTask(project, CreateTSAStart, UTILS_TASKS)
		PluginUtils.createTask(project, CreateReportLast, UTILS_TASKS)
		PluginUtils.createTask(project, CreateReportLastOld, UTILS_TASKS)
		PluginUtils.createTask(project, CreateMonthlyBaselines, UTILS_TASKS)
		PluginUtils.createTask(project, CreateMonthlyBaselinesOld, UTILS_TASKS)
	}

	static void createExtensions(Project project) {
		project.extensions.create(DashboardExtension.NAME, DashboardExtension)
		project.extensions.create(IssueQueryExtension.NAME, IssueQueryExtension)
	}
}
