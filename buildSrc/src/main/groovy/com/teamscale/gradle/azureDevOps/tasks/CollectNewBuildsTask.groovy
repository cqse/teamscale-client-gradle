package com.teamscale.gradle.azureDevOps.tasks

import com.teamscale.gradle.azureDevOps.config.BuildOptions
import com.teamscale.gradle.azureDevOps.data.Build
import com.teamscale.gradle.azureDevOps.data.Definition
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.time.Instant

import static com.teamscale.gradle.azureDevOps.utils.Logging.log

class CollectNewBuildsTask extends DefaultTask {
	@TaskAction
	def collect() {
		project.teamscale.azureDevOps.definitions.each { Definition definition ->
			def http = definition.getHttp()

			Instant minTime = definition.getMinLastProcessedTime().plusMillis(1)

			def builds = http.getBuildsForDefinition(definition.id, minTime).value.findResults {
				def build = new Build(it)
				if (isValidBuild(build, definition.getOptions())) {
					return build
				}
			}

			if (builds.size() == 0) {
				log("No new builds since $minTime", definition)
			} else {
				definition.getBuilds().addAll(builds)
				log("Found ${builds.size()} unprocessed build(s)", definition)
			}
		}
	}

	boolean isValidBuild(Build build, BuildOptions options) {
		// TODO: branchMapping
		// TODO: Constants for default-branch and `null` in, don't process the build
		return true
	}
}