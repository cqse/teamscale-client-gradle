package com.teamscale.gradle.teamscale


import com.teamscale.gradle.azureDevOps.config.AzureDevOps
import org.gradle.api.Action

class TeamscaleExtension {
	public static String NAME = "teamscale"

	TeamscaleClient http

	TeamscaleConfig config = new TeamscaleConfig()

	AzureDevOps azureDevOps

	void server(Action<? super TeamscaleConfig> action) {
		action.execute(config)
		http = new TeamscaleClient(config)
	}

	void azureDevOps(Action<? super AzureDevOps> action) {
		action.execute(azureDevOps)
	}
}
