package com.teamscale.gradle.azureDevOps.tasks.xaml

import com.teamscale.gradle.azureDevOps.data.XamlBuild
import com.teamscale.gradle.azureDevOps.data.XamlDefinition
import com.teamscale.gradle.azureDevOps.extensions.AzureDevOpsExtension
import com.teamscale.gradle.azureDevOps.tasks.base.UploadTestCoverageTask
import com.teamscale.gradle.azureDevOps.utils.ZipUtils
import com.teamscale.gradle.azureDevOps.utils.convert.MSCoverageConverter
import com.teamscale.gradle.teamscale.data.TeamscaleExtension

import java.nio.file.Path

/**
 * Tasks for fetching and uploading the test coverage inside of a XAML build archive.
 */
class UploadXamlTestCoverageTask extends UploadTestCoverageTask<XamlDefinition, XamlBuild> {
	final static String TASK_NAME = "uploadXamlTestCoverage"

	@Override
	void run(XamlDefinition definition, XamlBuild build) {
		def coverageOptions = definition.config.coverage

		List<File> coverageFiles
		if (coverageOptions.type == "MS_COVERAGE") {
			Path coverageFolder = ZipUtils.getMatchesPreservePaths(build.archive, coverageOptions)
			coverageFiles = [MSCoverageConverter.convert(coverageFolder, getCodeMergerPath()).toFile()]
		} else {
			coverageFiles = ZipUtils.getMatches(build.archive, definition.config.coverage).collect { it.toFile() }
		}

		upload(definition, build, coverageFiles, coverageOptions)
	}

	/**
	 * Get the path to the executable which can convert a .coverage file to an .xml
	 */
	protected String getCodeMergerPath() {
		def path = TeamscaleExtension.getFrom(project).azureDevOps.coverageMergerExePath

		assert path != null: "No coverage merger exe given! In order to use MS_COVERAGE you need " +
			"to provide a coverage merger exe in ${AzureDevOpsExtension.NAME} with 'coverageMergerPath \"<path>\"'. " +
			"MS_COVERAGE needs to be converted before it can be uploaded to Teamscale"

		assert (new File(path)).exists(): "Coverage merger exe at path $path does not exists"

		return path
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
