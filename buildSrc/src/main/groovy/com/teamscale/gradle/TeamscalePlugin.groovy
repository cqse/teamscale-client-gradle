package com.teamscale.gradle

import com.teamscale.gradle.azureDevOps.config.AzureDevOpsExtension
import com.teamscale.gradle.azureDevOps.tasks.*
import com.teamscale.gradle.azureDevOps.utils.logging.CustomTaskLogger
import com.teamscale.gradle.teamscale.TeamscaleExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

import static com.teamscale.gradle.azureDevOps.tasks.UploadTask.TASK_GROUP

class TeamscalePlugin implements Plugin<Project> {
	@Override
	void apply(Project project) {
		project.getGradle().useLogger(new CustomTaskLogger())

		TeamscaleExtension teamscale = project.extensions.create(TeamscaleExtension.NAME, TeamscaleExtension)
		teamscale[AzureDevOpsExtension.NAME] = new AzureDevOpsExtension(project)

		createAzureDevOpsTasks(project)
	}

	static createAzureDevOpsTasks(Project project) {
		def collectDefinitions = createTask(project, CollectBuildDefinitionsTask)
		def collectNewBuilds = createTask(project, CollectNewBuildsTask, null, collectDefinitions)

		// Upload Tasks
		createTask(project, UploadBuildStatusTask, TASK_GROUP, collectNewBuilds)
		createTask(project, UploadBuildFindingsTasks, TASK_GROUP, collectNewBuilds)
		createTask(project, UploadTestCoverageTask, TASK_GROUP, collectNewBuilds)
		createTask(project, UploadTestResultsTask, TASK_GROUP, collectNewBuilds)

		project.afterEvaluate {
			TeamscaleExtension teamscale = TeamscaleExtension.getFrom(project)

			assert teamscale.azureDevOps.cache != null: "No cache set. Please use the 'cacheDir <path>` method" +
				"inside of $AzureDevOpsExtension.NAME {}"

			// Configure the upload tasks
			project.gradle.taskGraph.whenReady {
				project.gradle.taskGraph.allTasks.each { task ->
					if (task instanceof UploadTask) {
						teamscale.azureDevOps.configuredUploadTasks.add(task.getUploadType())
					}
				}
			}
		}
	}

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