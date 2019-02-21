package com.teamscale.gradle.teamscale.data

import com.teamscale.gradle.azureDevOps.extensions.AzureDevOps
import com.teamscale.gradle.azureDevOps.extensions.DebugOptions
import com.teamscale.gradle.teamscale.TeamscaleClient
import org.gradle.api.Action
import org.gradle.api.Project

class TeamscaleExtension {
	public static String NAME = "teamscale"

	TeamscaleClient http

	TeamscaleConfig config = new TeamscaleConfig()

	AzureDevOps azureDevOps

	DebugOptions debug

	void server(Action<? super TeamscaleConfig> action) {
		action.execute(config)
		http = new TeamscaleClient(config)
	}

	void azureDevOps(Action<? super AzureDevOps> action) {
		action.execute(azureDevOps)
	}

	void debug(Action<? super DebugOptions> action) {
		action.execute(debug)
	}

	/** Return the teamscale extension for the given project */
	static TeamscaleExtension getFrom(Project project) {
		return project.extensions.getByName(NAME)
	}
}
