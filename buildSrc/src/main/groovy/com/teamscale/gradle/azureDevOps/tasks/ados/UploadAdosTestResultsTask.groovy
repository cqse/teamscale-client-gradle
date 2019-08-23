package com.teamscale.gradle.azureDevOps.tasks.ados

import com.teamscale.gradle.azureDevOps.data.AdosBuild
import com.teamscale.gradle.azureDevOps.data.AdosDefinition
import com.teamscale.gradle.azureDevOps.tasks.base.UploadTestResultsTask
import com.teamscale.gradle.azureDevOps.utils.AdosUtils
import com.teamscale.gradle.azureDevOps.utils.ReportLocationMatcher
import com.teamscale.gradle.teamscale.data.TeamscaleExtension

class UploadAdosTestResultsTask extends UploadTestResultsTask<AdosDefinition, AdosBuild> {
	final static String TASK_NAME = "uploadTestResults"

	@Override
	List<ReportLocationMatcher> getTestResultConfigurations(AdosDefinition definition) {
		return definition.options.tests.resultConfigs
	}

	@Override
	List<File> getResultFiles(AdosDefinition definition, AdosBuild build, ReportLocationMatcher config) {
		List<File> testResults
		if (config.mustSearchInArtifact()) {
			testResults = AdosUtils.getFilesFromBuildArtifact(definition, build, config)
		} else {
			testResults = AdosUtils.getFilesFromTestRuns(definition, build, config)
		}

		return testResults
	}

	@Override
	boolean isConfiguredForTask(AdosDefinition definition) {
		return (definition.options.tests != null && definition.options.tests.resultConfigs.size() > 0)
	}

	@Override
	List<AdosDefinition> getDefinitions() {
		return TeamscaleExtension.getFrom(project).azureDevOps.definitions
	}
}