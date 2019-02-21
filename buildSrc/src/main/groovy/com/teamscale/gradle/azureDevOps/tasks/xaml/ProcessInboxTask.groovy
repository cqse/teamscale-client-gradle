package com.teamscale.gradle.azureDevOps.tasks.xaml

import com.teamscale.gradle.azureDevOps.extensions.XamlDefinitionOptions
import com.teamscale.gradle.azureDevOps.extensions.XamlExtension
import com.teamscale.gradle.azureDevOps.extensions.XamlProjectConfig
import com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils
import com.teamscale.gradle.teamscale.data.TeamscaleExtension

import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.Paths

class ProcessInboxTask extends XamlTask {
	final static String TASK_NAME = "processInbox"

	@Override
	void process(Path buildDir, XamlProjectConfig adosProject, XamlDefinitionOptions definition) {
		XamlExtension xaml = TeamscaleExtension.getFrom(project).azureDevOps.xaml
		Path definitionInbox = getProjectDir(xaml.inbox.toString(), project.name, definition.name)

		int moved = 0
		Files.list(definitionInbox).each { Path file ->
			if (Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) {
				// TODO: check what happens if file is being copied
				file.renameTo(Paths.get(buildDir.toString(), file.fileName.toString()).toString())
				LoggingUtils.log("Moving new build to zipstore", definition.name, "$file.fileName")
				moved++
			}
		}

		if (moved > 0) {
			LoggingUtils.log("Moved $moved new build files", definition.name)
		} else {
			LoggingUtils.log("No new build files found", definition.name)
		}
	}
}
