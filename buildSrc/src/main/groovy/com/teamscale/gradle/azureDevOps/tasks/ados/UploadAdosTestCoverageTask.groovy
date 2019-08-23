package com.teamscale.gradle.azureDevOps.tasks.ados

import com.teamscale.gradle.azureDevOps.data.AdosBuild
import com.teamscale.gradle.azureDevOps.data.AdosDefinition
import com.teamscale.gradle.azureDevOps.tasks.base.UploadTestCoverageTask
import com.teamscale.gradle.azureDevOps.utils.AdosUtils
import com.teamscale.gradle.azureDevOps.utils.ReportLocationMatcher
import com.teamscale.gradle.teamscale.data.TeamscaleExtension

/**
 * Task handling the down- and uploading of the test coverage of the builds of the a configured definition.
 */
class UploadAdosTestCoverageTask extends UploadTestCoverageTask<AdosDefinition, AdosBuild> {
	final static String TASK_NAME = "uploadTestCoverage"

	@Override
	List<ReportLocationMatcher> getCoverageConfigurations(AdosDefinition definition) {
		return definition.options.tests.coverageConfigs
	}

	@Override
	List<File> getCoverageFiles(AdosDefinition definition, AdosBuild build, ReportLocationMatcher config) {
		List<File> coverageFiles
		// The code coverage can be downloaded from the coverage REST service call or from a published artifact
		if (config.mustSearchInArtifact()) {
			coverageFiles = AdosUtils.getFilesFromBuildArtifact(definition, build, config)
		} else {
			coverageFiles = definition.http.downloadTestCoverage(build.id)
			if (coverageFiles.isEmpty()) {
				coverageFiles = AdosUtils.getFilesFromTestRuns(definition, build, config)
			}
		}

		return coverageFiles
	}

	@Override
	boolean isConfiguredForTask(AdosDefinition definition) {
		return definition.options.tests && definition.options.tests.coverageConfigs.size() > 0
	}

	@Override
	List<AdosDefinition> getDefinitions() {
		return TeamscaleExtension.getFrom(project).azureDevOps.definitions
	}
}
