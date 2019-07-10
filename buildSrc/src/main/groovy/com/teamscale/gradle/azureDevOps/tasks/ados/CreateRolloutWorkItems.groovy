package com.teamscale.gradle.azureDevOps.tasks.ados

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class CreateRolloutWorkItems extends DefaultTask {
	private String applicationName = ""
	private int parentEpicId = -1

	@TaskAction
	def getParameters() {
		applicationName = "getEnvironmentVariable from Jenkins parameter"
		parentEpicId = Integer.parseInt("getEnvironmentVariable from Jenkins parameter")
		// Maybe as first step: hard code these values
	}

	@TaskAction
    def getAzureCredentials() {
		// Check other ADOS gradle tasks how to get the gradle parameters
	}

	@TaskAction
	def createFeatures() {
		// Research how the API looks like
	}

	@TaskAction
	def createBacklogItems() {
		// Research how the API looks like
	}

	@TaskAction
	def createTasks() {
		// Research how the API looks like
	}
}
