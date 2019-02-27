package com.teamscale.gradle.azureDevOps.tasks.ados

import com.teamscale.gradle.azureDevOps.data.AdosBuild
import com.teamscale.gradle.azureDevOps.data.AdosDefinition
import com.teamscale.gradle.azureDevOps.tasks.base.UploadTestCoverageTask
import com.teamscale.gradle.azureDevOps.utils.AdosUtils
import com.teamscale.gradle.teamscale.data.TeamscaleExtension

/**
 * Task handling the down- and uploading of the test coverage of the builds of the a configured definition.
 */
class UploadAdosTestCoverageTask extends UploadTestCoverageTask<AdosDefinition, AdosBuild> {
	final static String TASK_NAME = "uploadTestCoverage"

	@Override
	void run(AdosDefinition definition, AdosBuild build) {
		def coverageOptions = definition.options.tests.coverageOptions

		List<File> coverageFiles
		// The test coverage can be downloaded from the coverage REST service call or from a
		// published artifact
		if (coverageOptions.mustSearchInArtifact()) {
			coverageFiles = AdosUtils.getFilesFromBuildArtifact(definition, build, coverageOptions)
		} else {
			coverageFiles = definition.http.downloadTestCoverage(build.id)
			if (coverageFiles.isEmpty()) {
				coverageFiles = AdosUtils.getFilesFromTestRuns(definition, build, coverageOptions)
			}
		}

		upload(definition, build, coverageFiles, coverageOptions)
	}

	@Override
	boolean isConfiguredForTask(AdosDefinition definition) {
		return definition.options.tests && definition.options.tests.coverageOptions
	}

	@Override
	List<AdosDefinition> getDefinitions() {
		return TeamscaleExtension.getFrom(project).azureDevOps.definitions
	}
}
