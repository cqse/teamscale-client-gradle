package com.teamscale.gradle.azureDevOps.tasks.xaml

import com.teamscale.gradle.azureDevOps.data.XamlBuild
import com.teamscale.gradle.azureDevOps.extensions.XamlExtension
import com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils
import com.teamscale.gradle.teamscale.data.TeamscaleExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.nio.file.Files

class PruneZipStore extends DefaultTask {
	final static String TASK_NAME = "pruneZipStore"

	@TaskAction
	def action() {
		XamlExtension xaml = TeamscaleExtension.getFrom(project).azureDevOps.xaml

		if (!xaml.maxStoredZips) {
			LoggingUtils.warn("No maximum number set for build archives that should be retained!")
			return
		}

		xaml.definitions.each { definition ->
			if (definition.builds.size() > xaml.maxStoredZips) {
				def oldArchives = definition.builds.reverse().subList(xaml.maxStoredZips, definition.builds.size()) as List<XamlBuild>
				LoggingUtils.log("Removing ${oldArchives.size()} archive(s)", definition)
				oldArchives.each { Files.delete(it.archive) }
			}
		}
	}
}
