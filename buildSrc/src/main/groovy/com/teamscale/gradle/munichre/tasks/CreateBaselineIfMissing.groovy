package com.teamscale.gradle.munichre.tasks

import com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils
import com.teamscale.gradle.teamscale.TeamscaleClient

/**
 * Baseclass for creating baselines for a project if they are missing
 */
abstract class CreateBaselineIfMissing extends CreateBaseline {
	@Override
	void execute(TeamscaleClient http, String project) {
		String name = getBaselineName()
		if (hasBaseline(project, name)) {
			LoggingUtils.log("Skipping $project: \"$name\" already exists")
			return
		}

		createBaseline(project, name, getBaseline(project))
	}

	abstract String getBaselineName()

	long getBaseline(String project) {
		return getTimeStampOfFirstCommit(project)
	}
}
