package com.teamscale.gradle.teamscale


import com.teamscale.gradle.azureDevOps.config.AzureDevOpsExtension
import org.gradle.api.Action
import org.gradle.api.Project

class TeamscaleExtension {
	public static String NAME = "teamscale"

	TeamscaleClient http

	TeamscaleConfig config = new TeamscaleConfig()

	AzureDevOpsExtension azureDevOps

	void server(Action<? super TeamscaleConfig> action) {
		action.execute(config)
		http = new TeamscaleClient(config)
	}

	void azureDevOps(Action<? super AzureDevOpsExtension> action) {
		action.execute(azureDevOps)
	}

	static TeamscaleExtension getFrom(Project project) {
		return project.extensions.getByName(NAME)
	}
}
