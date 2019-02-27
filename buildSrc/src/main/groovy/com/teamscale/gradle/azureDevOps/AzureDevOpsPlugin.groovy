package com.teamscale.gradle.azureDevOps

import com.teamscale.gradle.PluginUtils
import com.teamscale.gradle.azureDevOps.extensions.AzureDevOps
import com.teamscale.gradle.azureDevOps.tasks.ados.UploadAdosBuildFindingsTask
import com.teamscale.gradle.azureDevOps.tasks.ados.UploadAdosBuildStatusTask
import com.teamscale.gradle.azureDevOps.tasks.ados.UploadAdosExternalReportsTask
import com.teamscale.gradle.azureDevOps.tasks.ados.UploadAdosReleaseTestResultsTasks
import com.teamscale.gradle.azureDevOps.tasks.ados.UploadAdosTestCoverageTask
import com.teamscale.gradle.azureDevOps.tasks.ados.UploadAdosTestResultsTask
import com.teamscale.gradle.azureDevOps.tasks.ados.CollectBuildDefinitionsTask
import com.teamscale.gradle.azureDevOps.tasks.ados.CollectNewBuildsTask
import com.teamscale.gradle.azureDevOps.tasks.xaml.ProcessBuildArchivesTask
import com.teamscale.gradle.azureDevOps.tasks.xaml.PruneZipStore
import com.teamscale.gradle.azureDevOps.tasks.xaml.UploadXamlBuildFindingsTask
import com.teamscale.gradle.azureDevOps.tasks.xaml.UploadXamlBuildStatusTask
import com.teamscale.gradle.azureDevOps.tasks.xaml.UploadXamlTestCoverageTask
import com.teamscale.gradle.azureDevOps.tasks.xaml.UploadXamlTestResultsTask
import com.teamscale.gradle.teamscale.data.TeamscaleExtension
import org.gradle.api.Project
import org.gradle.api.Task

import static com.teamscale.gradle.azureDevOps.tasks.base.AdosUploadTask.TASK_GROUP

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
		Task process = PluginUtils.createTask(project, ProcessBuildArchivesTask, XAML_TASKS)
		Task prune = PluginUtils.createTask(project, PruneZipStore, XAML_TASKS).dependsOn(process)

		def uploadTasks = []
		uploadTasks.add(PluginUtils.createTask(project, UploadXamlBuildStatusTask, XAML_TASKS).dependsOn(process).finalizedBy(prune))
		uploadTasks.add(PluginUtils.createTask(project, UploadXamlBuildFindingsTask, XAML_TASKS).dependsOn(process).finalizedBy(prune))
		uploadTasks.add(PluginUtils.createTask(project, UploadXamlTestResultsTask, XAML_TASKS).dependsOn(process).finalizedBy(prune))
		uploadTasks.add(PluginUtils.createTask(project, UploadXamlTestCoverageTask, XAML_TASKS).dependsOn(process).finalizedBy(prune))
		project.tasks.create("uploadXamlBuildInformation").dependsOn(uploadTasks as Object[]).finalizedBy(prune).setGroup(XAML_TASKS)
	}

	static void createBuildUploadTasks(Project project) {
		Task collectDefinitions = PluginUtils.createTask(project, CollectBuildDefinitionsTask, TASK_GROUP)
		Task collectNewBuilds = PluginUtils.createTask(project, CollectNewBuildsTask, TASK_GROUP, collectDefinitions)

		def uploadTasks = []
		uploadTasks.add(PluginUtils.createTask(project, UploadAdosBuildStatusTask, TASK_GROUP, collectNewBuilds))
		uploadTasks.add(PluginUtils.createTask(project, UploadAdosBuildFindingsTask, TASK_GROUP, collectNewBuilds))
		uploadTasks.add(PluginUtils.createTask(project, UploadAdosTestCoverageTask, TASK_GROUP, collectNewBuilds))
		uploadTasks.add(PluginUtils.createTask(project, UploadAdosTestResultsTask, TASK_GROUP, collectNewBuilds))
		uploadTasks.add(PluginUtils.createTask(project, UploadAdosReleaseTestResultsTasks, TASK_GROUP, collectNewBuilds))
		uploadTasks.add(PluginUtils.createTask(project, UploadAdosExternalReportsTask, TASK_GROUP, collectNewBuilds))
		project.tasks.create("uploadBuildInformation").dependsOn(uploadTasks as Object[]).setGroup(TASK_GROUP)
	}

	/** Assert that obligatory settings have been set */
	static void checkConfig(Project project) {
		TeamscaleExtension teamscale = TeamscaleExtension.getFrom(project)

		assert teamscale.azureDevOps.cache != null: "No cache set. Please use the 'cacheDir <path>` method" +
			"inside of $AzureDevOps.NAME {}"
	}
}
