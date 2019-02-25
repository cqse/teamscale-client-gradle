package com.teamscale.gradle.azureDevOps.tasks.xaml

import com.teamscale.gradle.azureDevOps.data.XamlBuild
import com.teamscale.gradle.azureDevOps.data.XamlDefinition
import com.teamscale.gradle.azureDevOps.tasks.base.UploadBuildStatusTask
import com.teamscale.gradle.teamscale.data.TeamscaleExtension

class UploadXamlBuildStatusTask extends UploadBuildStatusTask<XamlDefinition, XamlBuild> {
	final static String TASK_NAME = "uploadXamlBuildStatus"

	@Override
	void run(XamlDefinition definition, XamlBuild build) {
		upload(definition, build)
	}

	@Override
	List<XamlDefinition> getDefinitions() {
		return TeamscaleExtension.getFrom(project).azureDevOps.xaml.definitions
	}


	@Override
	protected boolean isConfiguredForTask(XamlDefinition definition) {
		return definition.config.errors
	}
}
