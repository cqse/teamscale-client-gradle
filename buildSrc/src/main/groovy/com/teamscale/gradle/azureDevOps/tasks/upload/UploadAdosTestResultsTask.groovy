package com.teamscale.gradle.azureDevOps.tasks.upload

import com.teamscale.gradle.azureDevOps.data.AdosBuild
import com.teamscale.gradle.azureDevOps.data.AdosDefinition
import com.teamscale.gradle.azureDevOps.tasks.base.UploadTestResultsTask
import com.teamscale.gradle.azureDevOps.utils.AdosUtils
import com.teamscale.gradle.teamscale.data.TeamscaleExtension

class UploadAdosTestResultsTask extends UploadTestResultsTask<AdosDefinition, AdosBuild> {
	final static String TASK_NAME = "uploadAdosTestResults"

	@Override
	void run(AdosDefinition definition, AdosBuild build) {
		def options = definition.options.tests.resultOptions

		// get test result files
		List<File> testResults
		if (options.mustSearchInArtifact()) {
			testResults = AdosUtils.getFilesFromBuildArtifact(definition, build, options)
		} else {
			testResults = AdosUtils.getFilesFromTestRuns(definition, build, options)
		}

		upload(definition, build, options, testResults)
	}

	@Override
	boolean isConfiguredForTask(AdosDefinition definition) {
		return (definition.options.tests != null && definition.options.tests.resultOptions)
	}

	@Override
	List<AdosDefinition> getDefinitions() {
		return TeamscaleExtension.getFrom(project).azureDevOps.definitions
	}
}