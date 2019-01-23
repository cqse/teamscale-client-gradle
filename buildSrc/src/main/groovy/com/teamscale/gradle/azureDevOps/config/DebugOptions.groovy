package com.teamscale.gradle.azureDevOps.config

import com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils
import com.teamscale.gradle.teamscale.data.TeamscaleExtension
import org.gradle.api.Project

class DebugOptions {
	public static final String NAME = "debug"

	Project project

	DebugOptions(project) {
		this.project = project
	}

	void disableTeamscaleUploads() {
		project.afterEvaluate {
			TeamscaleExtension teamscale = TeamscaleExtension.getFrom(project)

			if (teamscale.http) {
				teamscale.http.disableUploads()
				LoggingUtils.debug("Uploads to teamscale are disabled")
			}
		}
	}

	void disableCache() {
		project.afterEvaluate {
			TeamscaleExtension teamscale = TeamscaleExtension.getFrom(project)

			if (teamscale.azureDevOps) {
				teamscale.azureDevOps.cache.disable()
				LoggingUtils.debug("The cache for the azure dev ops jobs is disabled")
			}
		}
	}
}