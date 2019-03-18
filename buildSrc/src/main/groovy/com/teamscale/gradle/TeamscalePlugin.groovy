package com.teamscale.gradle

import com.teamscale.gradle.azureDevOps.AzureDevOpsPlugin
import com.teamscale.gradle.azureDevOps.extensions.AzureDevOpsExtension
import com.teamscale.gradle.azureDevOps.extensions.DebugOptions
import com.teamscale.gradle.azureDevOps.utils.logging.CustomTaskLogger
import com.teamscale.gradle.munichre.MunichRePlugin
import com.teamscale.gradle.teamscale.data.TeamscaleExtension
import com.teamscale.gradle.teamscale.tasks.dashboard.DashboardPlugin
import com.teamscale.gradle.teamscale.tasks.util.MutePartition
import org.gradle.api.Plugin
import org.gradle.api.Project

class TeamscalePlugin implements Plugin<Project> {
	public static final String UTIL_TASKS = "Teamscale Dashboard Utils"

	@Override
	void apply(Project project) {
		project.getGradle().useLogger(new CustomTaskLogger())

		TeamscaleExtension teamscale = project.extensions.create(TeamscaleExtension.NAME, TeamscaleExtension)

		teamscale[AzureDevOpsExtension.NAME] = new AzureDevOpsExtension(project)
		teamscale[DebugOptions.NAME] = new DebugOptions(project)

		MunichRePlugin.createExtensions(project)

		createProjectTasks(project, teamscale)
	}

	static void createProjectTasks(Project project, TeamscaleExtension teamscale) {
		project.afterEvaluate {
			// Only create the tasks for real projects
			if (!teamscale?.config?.project) {
				return
			}

			createUtilsTasks(project)
			AzureDevOpsPlugin.createTasks(project)
			DashboardPlugin.createTasks(project)
			MunichRePlugin.createTasks(project)
		}
	}

	static void createUtilsTasks(Project project) {
		PluginUtils.createTask(project, MutePartition, UTIL_TASKS)
	}
}