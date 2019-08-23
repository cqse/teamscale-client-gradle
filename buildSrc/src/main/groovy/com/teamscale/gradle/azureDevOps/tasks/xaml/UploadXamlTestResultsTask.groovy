package com.teamscale.gradle.azureDevOps.tasks.xaml

import com.teamscale.gradle.azureDevOps.data.XamlBuild
import com.teamscale.gradle.azureDevOps.data.XamlDefinition
import com.teamscale.gradle.azureDevOps.tasks.base.UploadTestResultsTask
import com.teamscale.gradle.azureDevOps.utils.ReportLocationMatcher
import com.teamscale.gradle.azureDevOps.utils.ZipUtils
import com.teamscale.gradle.teamscale.data.TeamscaleExtension

class UploadXamlTestResultsTask extends UploadTestResultsTask<XamlDefinition, XamlBuild> {
	final static String TASK_NAME = "uploadXamlTestResults"

	@Override
	List<ReportLocationMatcher> getTestResultConfigurations(XamlDefinition definition) {
		return definition.config.result
	}

	@Override
	List<File> getResultFiles(XamlDefinition definition, XamlBuild build, ReportLocationMatcher config) {
		return ZipUtils.getMatches(build.archive, config).collect { it.toFile() }
	}

	@Override
	boolean isConfiguredForTask(XamlDefinition definition) {
		return definition.config.result.size() > 0
	}

	@Override
	List<XamlDefinition> getDefinitions() {
		return TeamscaleExtension.getFrom(project).azureDevOps.xaml.definitions
	}
}
