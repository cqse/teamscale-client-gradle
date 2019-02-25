package com.teamscale.gradle.azureDevOps.tasks.xaml

import com.teamscale.gradle.azureDevOps.extensions.XamlExtension
import com.teamscale.gradle.teamscale.data.TeamscaleExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class PruneZipStore extends DefaultTask {
	final static String TASK_NAME = "pruneZipStore"

	@TaskAction
	def action() {
		XamlExtension xaml = TeamscaleExtension.getFrom(project).azureDevOps.xaml

		xaml.definitions.each { definition ->
			//
		}
	}
}
