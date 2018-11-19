package eu.cqse.vsts

import eu.cqse.vsts.config.VstsPluginExtension
import eu.cqse.vsts.tasks.CollectBuildDefinitions
import org.gradle.api.Plugin
import org.gradle.api.Project

class VstsPlugin implements Plugin<Project> {
	void apply(Project project) {
		project.extensions.create("vsts", VstsPluginExtension)
		project.tasks.create("collectTest", CollectBuildDefinitions)
	}
}