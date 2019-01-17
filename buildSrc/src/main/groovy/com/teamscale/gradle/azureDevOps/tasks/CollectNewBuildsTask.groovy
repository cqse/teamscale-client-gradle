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

			checkMaxTimeBetweenBuilds(definition)

			if (builds.size() == 0) {
				log("No unprocessed builds since $minTime", definition)
			} else {
				log("Found ${builds.size()} unprocessed build(s)", definition)
				definition.getBuilds().addAll(builds)
			}
		}
	}

	/**
	 * Checks the time of the last build of the given definition. If this exceeds a certain number
	 * of days, which are defined in the definition options, then a warning will be logged.
	 *
	 * This check needs to run even if unprocessed builds could be found, because the release tests uploads
	 * must always check the latest build even if it has already been processed.
	 */
	protected static void checkMaxTimeBetweenBuilds(Definition definition) {
		if(!definition.lastCompletedTime) {
			return
		}

		int daysBeforeWarning = definition.options.maxDaysBetweenBuilds
		long daysAfterLastBuild = Duration.between(definition.lastCompletedTime, Instant.now()).toDays()
		if (daysAfterLastBuild > daysBeforeWarning) {
			warn("Last build for '$definition.name' was executed $daysAfterLastBuild " +
				"days ago (max: $daysBeforeWarning)!")
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