package com.teamscale.gradle

import com.teamscale.gradle.azureDevOps.config.AzureDevOpsExtension
import com.teamscale.gradle.azureDevOps.config.DebugOptions
import com.teamscale.gradle.azureDevOps.tasks.CollectBuildDefinitionsTask
import com.teamscale.gradle.azureDevOps.tasks.CollectNewBuildsTask
import com.teamscale.gradle.azureDevOps.tasks.upload.UploadBuildFindingsTasks
import com.teamscale.gradle.azureDevOps.tasks.upload.UploadBuildStatusTask
import com.teamscale.gradle.azureDevOps.tasks.upload.UploadExternalReportsTask
import com.teamscale.gradle.azureDevOps.tasks.upload.UploadReleaseTestResultsTasks
import com.teamscale.gradle.azureDevOps.tasks.upload.UploadTestCoverageTask
import com.teamscale.gradle.azureDevOps.tasks.upload.UploadTestResultsTask
import com.teamscale.gradle.azureDevOps.utils.logging.CustomTaskLogger
import com.teamscale.gradle.teamscale.TeamscaleExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

import static com.teamscale.gradle.azureDevOps.tasks.upload.UploadTask.TASK_GROUP

class TeamscalePlugin implements Plugin<Project> {
	@Override
	void apply(Project project) {
		project.getGradle().useLogger(new CustomTaskLogger())

		TeamscaleExtension teamscale = project.extensions.create(TeamscaleExtension.NAME, TeamscaleExtension)
		teamscale[AzureDevOpsExtension.NAME] = new AzureDevOpsExtension(project)
		teamscale[DebugOptions.NAME] = new DebugOptions(project)

		createAzureDevOpsTasks(project)
	}

	static createAzureDevOpsTasks(Project project) {
		def collectDefinitions = createTask(project, CollectBuildDefinitionsTask)
		def collectNewBuilds = createTask(project, CollectNewBuildsTask, null, collectDefinitions)

		def uploadTasks = []
		uploadTasks.add(createTask(project, UploadBuildStatusTask, TASK_GROUP, collectNewBuilds))
		uploadTasks.add(createTask(project, UploadBuildFindingsTasks, TASK_GROUP, collectNewBuilds))
		uploadTasks.add(createTask(project, UploadTestCoverageTask, TASK_GROUP, collectNewBuilds))
		uploadTasks.add(createTask(project, UploadTestResultsTask, TASK_GROUP, collectNewBuilds))
		uploadTasks.add(createTask(project, UploadReleaseTestResultsTasks, TASK_GROUP, collectNewBuilds))
		uploadTasks.add(createTask(project, UploadExternalReportsTask, TASK_GROUP, collectNewBuilds))
		project.tasks.create("uploadBuildInformation").dependsOn(uploadTasks as Object[]).group(TASK_GROUP)

		project.afterEvaluate {
			checkConfig(project)
		}
	}

	/** Assert that obligatory settings have been set */
	static void checkConfig(Project project) {
		TeamscaleExtension teamscale = TeamscaleExtension.getFrom(project)

		assert teamscale.azureDevOps.cache != null: "No cache set. Please use the 'cacheDir <path>` method" +
			"inside of $AzureDevOpsExtension.NAME {}"
	}

	/**
	 * Create a task for the project.
	 * Optional parameters can set a group and dependencies to other tasks
	 */
	static createTask(Project project, Class cls, String group = null, Object... dependsOn) {
		Task task = project.tasks.create((String) cls.NAME, cls)
		if (dependsOn) {
			task.dependsOn(dependsOn)
		}

		if (group) {
			task.group = group
		}

		return task
	}
}