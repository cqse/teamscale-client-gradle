package com.teamscale.gradle.azureDevOps.tasks.xaml

import com.teamscale.gradle.azureDevOps.data.XamlBuild
import com.teamscale.gradle.azureDevOps.data.XamlDefinition
import com.teamscale.gradle.azureDevOps.tasks.base.UploadBuildFindingsTask
import com.teamscale.gradle.azureDevOps.utils.ZipUtils
import com.teamscale.gradle.azureDevOps.utils.loganalyzer.ILogAnalyzer
import com.teamscale.gradle.azureDevOps.utils.loganalyzer.LogAnalyzerFactory
import com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils
import com.teamscale.gradle.teamscale.data.TeamscaleExtension
import com.teamscale.gradle.teamscale.data.TeamscaleFinding

import java.nio.file.Path

class UploadXamlBuildFindingsTask extends UploadBuildFindingsTask<XamlDefinition, XamlBuild> {
	final static String TASK_NAME = "uploadXamlBuildFindings"
	public static final String REJECT_REASON = "No parsing of build findings configured"

	@Override
	void run(XamlDefinition definition, XamlBuild build) {
		List<Path> matches = ZipUtils.getMatchesInArchive(build.archive, definition.config.warnings)

		if (matches.size() != 1) {
			LoggingUtils.warn("Found ${matches.size()} matches for $definition.config.warnings, but expected " +
				"exactly one")
			return
		}

		Path warningsFile = matches.get(0)

		ILogAnalyzer logAnalyzer = LogAnalyzerFactory.getFor(definition.config.warnings.type)
		Set<TeamscaleFinding> findings = warningsFile.readLines().findResults { line ->
			logAnalyzer.analyze(line)
		}

		upload(definition, build, findings)
	}

	@Override
	String getRejectReason() {
		return REJECT_REASON
	}

	@Override
	List<XamlDefinition> getDefinitions() {
		return TeamscaleExtension.getFrom(project).azureDevOps.xaml.definitions
	}

	@Override
	protected boolean isConfiguredForTask(XamlDefinition definition) {
		return definition.config.warnings
	}
}
