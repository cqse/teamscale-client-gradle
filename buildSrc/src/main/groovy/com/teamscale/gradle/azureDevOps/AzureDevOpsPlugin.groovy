package com.teamscale.gradle.azureDevOps

import com.teamscale.gradle.PluginUtils
import com.teamscale.gradle.azureDevOps.extensions.AzureDevOps
import com.teamscale.gradle.azureDevOps.tasks.CollectBuildDefinitionsTask
import com.teamscale.gradle.azureDevOps.tasks.CollectNewBuildsTask
import com.teamscale.gradle.azureDevOps.tasks.upload.*
import com.teamscale.gradle.azureDevOps.tasks.xaml.ProcessInboxTask
import com.teamscale.gradle.azureDevOps.tasks.xaml.PruneZipStore
import com.teamscale.gradle.azureDevOps.tasks.xaml.UploadXamlBuildInformation
import com.teamscale.gradle.teamscale.data.TeamscaleExtension
import org.gradle.api.Project
import org.gradle.api.Task

import static com.teamscale.gradle.azureDevOps.tasks.upload.UploadTask.TASK_GROUP

class AzureDevOpsPlugin {
	public static final String XAML_TASKS = "Azure XAML Build Information Upload"

	static void create(Project project) {
		project.afterEvaluate {
			TeamscaleExtension teamscale = TeamscaleExtension.getFrom(project)

			// Only create the tasks for real projects
			if (!teamscale?.config?.project) {
				return
			}

			checkConfig(project)
			createBuildUploadTasks(project)
			createXamlTasks(project)
		}
	}

	static void createXamlTasks(Project project) {
		Task processInbox = PluginUtils.createTask(project, ProcessInboxTask, XAML_TASKS)
		Task prune = PluginUtils.createTask(project, PruneZipStore, XAML_TASKS)
		PluginUtils.createTask(project, UploadXamlBuildInformation, XAML_TASKS).dependsOn(processInbox).finalizedBy(prune)
	}

	static void createBuildUploadTasks(Project project) {
		Task collectDefinitions = PluginUtils.createTask(project, CollectBuildDefinitionsTask, TASK_GROUP)
		Task collectNewBuilds = PluginUtils.createTask(project, CollectNewBuildsTask, TASK_GROUP, collectDefinitions)

		def uploadTasks = []
		uploadTasks.add(PluginUtils.createTask(project, UploadBuildStatusTask, TASK_GROUP, collectNewBuilds))
		uploadTasks.add(PluginUtils.createTask(project, UploadBuildFindingsTasks, TASK_GROUP, collectNewBuilds))
		uploadTasks.add(PluginUtils.createTask(project, UploadTestCoverageTask, TASK_GROUP, collectNewBuilds))
		uploadTasks.add(PluginUtils.createTask(project, UploadTestResultsTask, TASK_GROUP, collectNewBuilds))
		uploadTasks.add(PluginUtils.createTask(project, UploadReleaseTestResultsTasks, TASK_GROUP, collectNewBuilds))
		uploadTasks.add(PluginUtils.createTask(project, UploadExternalReportsTask, TASK_GROUP, collectNewBuilds))
		project.tasks.create("uploadBuildInformation").dependsOn(uploadTasks as Object[]).setGroup(TASK_GROUP)
	}

	/** Assert that obligatory settings have been set */
	static void checkConfig(Project project) {
		TeamscaleExtension teamscale = TeamscaleExtension.getFrom(project)

		assert teamscale.azureDevOps.cache != null: "No cache set. Please use the 'cacheDir <path>` method" +
			"inside of $AzureDevOps.NAME {}"
	}
}
