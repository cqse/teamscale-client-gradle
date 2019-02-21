package com.teamscale.gradle.azureDevOps.tasks.xaml

import com.teamscale.gradle.azureDevOps.extensions.XamlDefinitionOptions
import com.teamscale.gradle.azureDevOps.extensions.XamlExtension
import com.teamscale.gradle.azureDevOps.extensions.XamlProjectConfig
import com.teamscale.gradle.teamscale.data.TeamscaleExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

abstract class XamlTask extends DefaultTask {

	@TaskAction
	def action() {
		XamlExtension xaml = TeamscaleExtension.getFrom(project).azureDevOps.xaml

		assert xaml.inbox: "No inbox defined for the XAML build zips"
		assert xaml.zipStore: "No zip store defined for the XAML build zips"

		xaml.organizations.values().each { organization ->
			organization.projects.values().each { project ->
				project.definitions.values().each { definition ->
					Path buildDir = getProjectDir(xaml.zipStore.toString(), project.name, definition.name)
					process(buildDir, project, definition)
				}
			}
		}
	}

	abstract void process(Path buildDir, XamlProjectConfig adosProject, XamlDefinitionOptions definition)

	protected static Path getProjectDir(String basePath, String projectName, String definitionName) {
		Path path = Paths.get(basePath, projectName, definitionName)

		if (!Files.exists(path)) {
			Files.createDirectories(path)
		}

		if (!Files.isDirectory(path)) {
			// TODO: log warning that this is not a directory for some reason
		}

		return path
	}
}
