package com.teamscale.gradle.azureDevOps.tasks

import com.teamscale.gradle.azureDevOps.extensions.AzureDevOps
import com.teamscale.gradle.azureDevOps.data.AdosBuild
import com.teamscale.gradle.azureDevOps.data.AdosDefinition
import com.teamscale.gradle.azureDevOps.tasks.base.AdosUploadTask
import com.teamscale.gradle.teamscale.data.TeamscaleExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.time.Duration
import java.time.Instant

import static com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils.log
import static com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils.warn

class CollectNewBuildsTask extends DefaultTask {
	static String TASK_NAME = "collectNewBuilds"

	@TaskAction
	def collect() {
		AzureDevOps ados = TeamscaleExtension.getFrom(project).azureDevOps

		ados.definitions.each { AdosDefinition definition ->
			def http = definition.getHttp()

			Instant minTime = definition.getMinLastProcessedTimeFor(getConfiguredTaskTypes(definition)).plusMillis(1)

			List builds = http.getBuildsForDefinition(definition.id, minTime).findResults { Map data ->
				def build = new AdosBuild(data, definition.getOptions().getBranchMapping())
				if (build.targetBranch != null) {
					return build
				}
			}

			checkMaxTimeBetweenBuilds(definition, builds, minTime)

			if (builds.isEmpty()) {
				if (minTime.minusMillis(1) == Instant.EPOCH) {
					warn("No builds found for [$definition.name] which match the provided branch mapping")
				} else {
					log("No unprocessed builds since $minTime", definition)
				}
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
	protected static void checkMaxTimeBetweenBuilds(AdosDefinition definition, List<AdosBuild> builds, Instant lastProcessed) {
		if (!definition.lastCompletedTime) {
			return
		}

		def lastBuildTime = lastProcessed
		if (builds.size() > 0) {
			lastBuildTime = builds.finishTime.max()
		} else if (lastProcessed.minusMillis(1) == Instant.EPOCH) {
			return
		}

		int daysBeforeWarning = definition.options.maxDaysBetweenBuilds
		long daysAfterLastBuild = Duration.between(lastBuildTime, Instant.now()).toDays()
		if (daysAfterLastBuild > daysBeforeWarning) {
			warn("Last processable build for [$definition.name] was executed $daysAfterLastBuild " +
				"days ago (max: $daysBeforeWarning)!")
		}
	}

	/** Fetches the EBuildInformationType for all tasks which are called and are configured for the builds */
	List<EBuildInformationType> getConfiguredTaskTypes(AdosDefinition definition) {
		def taskTypes = []

		project.gradle.taskGraph.allTasks.each { task ->
			if (task instanceof AdosUploadTask && task.isConfiguredForTask(definition)) {
				taskTypes.add(task.getUploadType())
			}
		}

		return taskTypes
	}
}