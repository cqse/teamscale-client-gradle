package com.teamscale.gradle

import com.teamscale.gradle.azureDevOps.AzureDevOpsPlugin
import com.teamscale.gradle.azureDevOps.extensions.AzureDevOps
import com.teamscale.gradle.azureDevOps.extensions.DebugOptions
import com.teamscale.gradle.azureDevOps.utils.logging.CustomTaskLogger
import com.teamscale.gradle.munichre.MunichRePlugin
import com.teamscale.gradle.teamscale.data.TeamscaleExtension
import com.teamscale.gradle.teamscale.tasks.dashboard.DashboardPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class TeamscalePlugin implements Plugin<Project> {
	@Override
	void apply(Project project) {
		project.getGradle().useLogger(new CustomTaskLogger())

		TeamscaleExtension teamscale = project.extensions.create(TeamscaleExtension.NAME, TeamscaleExtension)

		teamscale[AzureDevOps.NAME] = new AzureDevOps(project)
		teamscale[DebugOptions.NAME] = new DebugOptions(project)

		AzureDevOpsPlugin.create(project)
		DashboardPlugin.create(project)
		MunichRePlugin.create(project)
	}
}