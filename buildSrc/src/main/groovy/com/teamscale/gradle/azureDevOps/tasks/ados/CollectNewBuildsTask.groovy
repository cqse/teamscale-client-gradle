package com.teamscale.gradle.azureDevOps.tasks.ados

import com.teamscale.gradle.azureDevOps.data.AdosBuild
import com.teamscale.gradle.azureDevOps.data.AdosDefinition
import com.teamscale.gradle.azureDevOps.extensions.AzureDevOpsExtension
import com.teamscale.gradle.azureDevOps.tasks.EBuildInformationType
import com.teamscale.gradle.azureDevOps.tasks.base.UploadTask
import com.teamscale.gradle.teamscale.data.TeamscaleExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.time.Duration
import java.time.Instant

import static com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils.log
import static com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils.warn

class CollectNewBuildsTask extends DefaultTask {
	static String TASK_NAME = "collectNewBuilds"
	public static final int TIME_DELTA = 10

	@TaskAction
	def collect() {
		AzureDevOpsExtension ados = TeamscaleExtension.getFrom(project).azureDevOps

		ados.definitions.each { AdosDefinition definition ->
			def http = definition.getHttp()

			Instant minTime = definition.getMinLastProcessedTimeFor(getConfiguredTaskTypes(definition)).plusMillis(TIME_DELTA)

			List<Map> allBuilds
			try {
				allBuilds = http.getBuildsForDefinition(definition.id, minTime)
			} catch (RuntimeException | SocketTimeoutException e) {
				if (e instanceof SocketTimeoutException || e.getCause() instanceof SocketTimeoutException) {
					warn("Timeout while fetching builds", definition)
					return
				}
				throw e
			}

			if(allBuilds.isEmpty() && definition.lastCompletedTime == null) {
				log("No builds run yet", definition)
				return
			}

			List<AdosBuild> builds = allBuilds.findResults { Map data ->
				def build = new AdosBuild(data, definition.getOptions().getBranchMapping())
				if (build.targetBranch != null) {
					return build
				}
			}

			checkMaxTimeBetweenBuilds(definition, builds, minTime)

			if (builds.isEmpty()) {
				if (minTime.minusMillis(TIME_DELTA) == Instant.EPOCH) {
					warn("No builds found which match the provided branch mapping", definition)
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

		Instant lastBuildTime = lastProcessed
		if (builds.size() > 0) {
			lastBuildTime = builds.finishTime.max()
		} else if (lastProcessed.minusMillis(1) == Instant.EPOCH) {
			return
		}

		int daysBeforeWarning = definition.options.maxDaysBetweenBuilds
		long daysAfterLastBuild = Duration.between(lastBuildTime, Instant.now()).toDays()
		if (daysAfterLastBuild > daysBeforeWarning) {
			// Removed as a warning as this is not really actionable and happens in a majority of the projects
			log("Last processable build for [$definition.name][$definition.id] was executed $daysAfterLastBuild " +
				"days ago (max: $daysBeforeWarning)!")
		}
	}

	/** Fetches the EBuildInformationType for all tasks which are called and are configured for the builds */
	List<EBuildInformationType> getConfiguredTaskTypes(AdosDefinition definition) {
		def taskTypes = []

		project.gradle.taskGraph.allTasks.each { task ->
			if (task instanceof UploadTask && task.isConfiguredForTask(definition)) {
				taskTypes.add(task.getUploadType())
			}
		}

		return taskTypes
	}
}