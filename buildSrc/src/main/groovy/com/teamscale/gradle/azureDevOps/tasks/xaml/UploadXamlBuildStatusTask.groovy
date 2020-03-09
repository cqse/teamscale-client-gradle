package com.teamscale.gradle.azureDevOps.tasks.xaml

import com.teamscale.gradle.azureDevOps.client.AzureDevOpsClient
import com.teamscale.gradle.azureDevOps.data.IBuild
import com.teamscale.gradle.azureDevOps.data.IDefinition
import com.teamscale.gradle.azureDevOps.data.XamlBuild
import com.teamscale.gradle.azureDevOps.data.XamlDefinition
import com.teamscale.gradle.azureDevOps.tasks.base.UploadBuildStatusTask
import com.teamscale.gradle.teamscale.data.TeamscaleExtension

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
	String getBuildUrl(IDefinition definition, IBuild build) {
		return "";
		// Id is not fetched from ADOS
		//return String.format("%s%s/%s/_build/xaml?buildId=%s", AzureDevOpsClient.URL, definition.organization, definition.project, build.id)
	}
}
