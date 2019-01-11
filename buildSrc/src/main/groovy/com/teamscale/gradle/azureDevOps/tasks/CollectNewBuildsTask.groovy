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

	/**
	 * If a build hasn't been processed in this amount of days, a warning
	 * will be logged.
	 */
	static int DAYS_THRESHOLD = 30

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

				if (Duration.between(minTime, Instant.now()).toDays() > DAYS_THRESHOLD) {
					warn("Last build was processed over $DAYS_THRESHOLD days ago!")
				}
			} else {
				log("Found ${builds.size()} unprocessed build(s)", definition)
				definition.getBuilds().addAll(builds)
			}
		}
	}

	/** */
	List<EBuildInformationType> getConfiguredTaskTypes(Definition definition) {
		def taskTypes = []

		project.gradle.taskGraph.allTasks.each { task ->
			if (task instanceof UploadTask && task.isConfiguredForTask(definition)) {
				taskTypes.add(task.getUploadType())
			}
		}

		println(taskTypes)
		return taskTypes
	}
}