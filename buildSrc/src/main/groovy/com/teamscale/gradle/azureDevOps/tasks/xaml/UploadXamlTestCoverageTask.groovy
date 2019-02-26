package com.teamscale.gradle.azureDevOps.tasks.xaml

import com.teamscale.gradle.azureDevOps.data.XamlBuild
import com.teamscale.gradle.azureDevOps.data.XamlDefinition
import com.teamscale.gradle.azureDevOps.tasks.base.UploadTestCoverageTask
import com.teamscale.gradle.azureDevOps.utils.ZipUtils
import com.teamscale.gradle.teamscale.data.TeamscaleExtension

class UploadXamlTestCoverageTask extends UploadTestCoverageTask<XamlDefinition, XamlBuild> {
	final static String TASK_NAME = "uploadXamlTestCoverage"

	@Override
	void run(XamlDefinition definition, XamlBuild build) {
		def coverageOptions = definition.config.coverage
		List<File> coverageFiles = ZipUtils.getMatchesInArchive(build.archive, definition.config.coverage)
			.collect { it.toFile() }

		upload(definition, build, coverageFiles, coverageOptions)
	}

	@Override
	List<XamlDefinition> getDefinitions() {
		return TeamscaleExtension.getFrom(project).azureDevOps.xaml.definitions
	}

	@Override
	boolean isConfiguredForTask(XamlDefinition definition) {
		return definition.config.coverage
	}
}
