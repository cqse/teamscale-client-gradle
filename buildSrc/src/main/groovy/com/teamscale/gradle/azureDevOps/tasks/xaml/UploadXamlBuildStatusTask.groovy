package com.teamscale.gradle.azureDevOps.tasks.xaml

import com.teamscale.gradle.azureDevOps.data.XamlBuild
import com.teamscale.gradle.azureDevOps.data.XamlDefinition
import com.teamscale.gradle.azureDevOps.tasks.base.UploadBuildStatusTask
import com.teamscale.gradle.teamscale.data.TeamscaleExtension

import static com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils.log

class UploadXamlBuildStatusTask extends UploadBuildStatusTask<XamlDefinition, XamlBuild> {
	final static String TASK_NAME = "uploadXamlBuildStatus"

	@Override
	void run(XamlDefinition definition, XamlBuild build) {
		upload(definition, build, definition.config.errors)
	}

	@Override
	List<XamlDefinition> getDefinitions() {
		return TeamscaleExtension.getFrom(project).azureDevOps.xaml.definitions
	}

	@Override
	boolean isConfiguredForTask(XamlDefinition definition) {
		return definition.config.errors
	}

	@Override
	protected boolean canBeProcessed(XamlDefinition definition, XamlBuild build) {
		// Always process
		return true
	}
}
