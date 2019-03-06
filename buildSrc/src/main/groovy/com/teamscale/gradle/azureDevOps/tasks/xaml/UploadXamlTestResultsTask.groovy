package com.teamscale.gradle.azureDevOps.tasks.xaml

import com.teamscale.gradle.azureDevOps.data.XamlBuild
import com.teamscale.gradle.azureDevOps.data.XamlDefinition
import com.teamscale.gradle.azureDevOps.tasks.base.UploadTestResultsTask
import com.teamscale.gradle.azureDevOps.utils.ZipUtils
import com.teamscale.gradle.teamscale.data.TeamscaleExtension

class UploadXamlTestResultsTask extends UploadTestResultsTask<XamlDefinition, XamlBuild> {
	final static String TASK_NAME = "uploadXamlTestResults"

	@Override
	void run(XamlDefinition definition, XamlBuild build) {
		def options = definition.config.result
		List<File> testResults = ZipUtils.getMatches(build.archive, options).collect { it.toFile() }

		upload(definition, build, options, testResults)
	}

	@Override
	boolean isConfiguredForTask(XamlDefinition definition) {
		return definition.config.result
	}

	@Override
	List<XamlDefinition> getDefinitions() {
		return TeamscaleExtension.getFrom(project).azureDevOps.xaml.definitions
	}
}
