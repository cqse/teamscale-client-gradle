package com.teamscale.gradle.azureDevOps.tasks

import com.teamscale.gradle.azureDevOps.config.AzureDevOpsExtension
import com.teamscale.gradle.azureDevOps.data.Build
import com.teamscale.gradle.azureDevOps.data.Definition
import com.teamscale.gradle.teamscale.TeamscaleExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.time.Duration
import java.time.Instant

import static com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils.log
import static com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils.warn

class CollectNewBuildsTask extends DefaultTask {
	static String NAME = "collectNewBuilds"

	@TaskAction
	def collect() {
		AzureDevOpsExtension ados = TeamscaleExtension.getFrom(project).azureDevOps

		ados.definitions.each { Definition definition ->
			def http = definition.getHttp()

			Instant minTime = definition.getMinLastProcessedTimeFor(getConfiguredTaskTypes(definition)).plusMillis(1)

			List builds = http.getBuildsForDefinition(definition.id, minTime).findResults { Map data ->
				def build = new Build(data, definition.getOptions().getBranchMapping())
				if (build.targetBranch != null) {
					return build
				}
			}

			if (builds.size() == 0) {
				log("No unprocessed builds since $minTime", definition)

				int daysBeforeWarning = definition.options.maxDaysBetweenBuilds
				if (Duration.between(minTime, Instant.now()).toDays() > daysBeforeWarning) {
					warn("Last build for $definition.name was executed over $daysBeforeWarning days ago!")
				}
			} else {
				log("Found ${builds.size()} unprocessed build(s)", definition)
				definition.getBuilds().addAll(builds)
			}
		}
	}

	/** Fetches the EBuildInformationType for all tasks which are called and are configured for the builds */
	List<EBuildInformationType> getConfiguredTaskTypes(Definition definition) {
		def taskTypes = []

		project.gradle.taskGraph.allTasks.each { task ->
			if (task instanceof UploadTask && task.isConfiguredForTask(definition)) {
				taskTypes.add(task.getUploadType())
			}
		}

		return taskTypes
	}
}