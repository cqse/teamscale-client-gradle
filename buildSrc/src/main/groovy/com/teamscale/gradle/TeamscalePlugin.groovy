package com.teamscale.gradle

import com.teamscale.gradle.azureDevOps.config.AzureDevOps
import com.teamscale.gradle.azureDevOps.tasks.CollectBuildDefinitionsTask
import com.teamscale.gradle.azureDevOps.tasks.CollectNewBuildsTask
import com.teamscale.gradle.azureDevOps.tasks.UploadTask
import com.teamscale.gradle.azureDevOps.tasks.UploadTestCoverageTask
import com.teamscale.gradle.azureDevOps.tasks.UploadBuildFindingsTasks
import com.teamscale.gradle.azureDevOps.tasks.UploadBuildStatusTask
import com.teamscale.gradle.azureDevOps.tasks.UploadTestResultsTask
import com.teamscale.gradle.azureDevOps.utils.logging.CustomTaskLogger
import com.teamscale.gradle.teamscale.TeamscaleExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class TeamscalePlugin implements Plugin<Project> {
	@Override
	void apply(Project project) {
		project.getGradle().useLogger(new CustomTaskLogger())

		TeamscaleExtension teamscale = project.extensions.create(TeamscaleExtension.NAME, TeamscaleExtension)
		teamscale[AzureDevOps.NAME] = new AzureDevOps(project)

		createAzureDevOpsTasks(project)
	}

	static createAzureDevOpsTasks(Project project) {
		def collectDefinitions = project.tasks.create(CollectBuildDefinitionsTask.NAME, CollectBuildDefinitionsTask)
		def collectNewBuilds = project.tasks.create(CollectNewBuildsTask.NAME, CollectNewBuildsTask).dependsOn(collectDefinitions)

		// Upload Tasks
		project.tasks.create(UploadBuildStatusTask.NAME, UploadBuildStatusTask).dependsOn(collectNewBuilds)
		project.tasks.create(UploadTestResultsTask.NAME, UploadTestResultsTask).dependsOn(collectNewBuilds)
		project.tasks.create(UploadTestCoverageTask.NAME, UploadTestCoverageTask).dependsOn(collectNewBuilds)
		project.tasks.create(UploadBuildFindingsTasks.NAME, UploadBuildFindingsTasks).dependsOn(collectNewBuilds)

		project.afterEvaluate {
			TeamscaleExtension teamscale = TeamscaleExtension.getFrom(project)

			assert teamscale.azureDevOps.cache != null: "No cache set. Please use the 'cacheDir <path>` " +
				"inside of $AzureDevOps.NAME {}"

			// Configure the upload tasks
			project.gradle.taskGraph.whenReady {
				project.gradle.taskGraph.allTasks.each { task ->
					if(task instanceof UploadTask) {
						teamscale.azureDevOps.configuredUploadTasks.add(task.getUploadType())
					}
				}
			}
		}
	}
}