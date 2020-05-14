package com.teamscale.gradle.azureDevOps.tasks.ados

import com.teamscale.gradle.azureDevOps.client.AzureDevOpsClient
import com.teamscale.gradle.azureDevOps.data.AdosBuild
import com.teamscale.gradle.azureDevOps.data.AdosDefinition
import com.teamscale.gradle.azureDevOps.data.IBuild
import com.teamscale.gradle.azureDevOps.data.IDefinition
import com.teamscale.gradle.azureDevOps.tasks.base.UploadBuildStatusTask
import com.teamscale.gradle.teamscale.data.TeamscaleExtension

/**
 * Determines and uploads the status of a XAML build, which is in the form of a zip archive.
 */
class UploadAdosBuildStatusTask extends UploadBuildStatusTask<AdosDefinition, AdosBuild> {
	public final static String TASK_NAME = "uploadBuildStatus"

	@Override
	void run(AdosDefinition definition, AdosBuild build) {
		upload(definition, build)
	}

	@Override
	boolean isConfiguredForTask(AdosDefinition definition) {
		// Build Status is always uploaded
		return true
	}

	@Override
	List<AdosDefinition> getDefinitions() {
		return TeamscaleExtension.getFrom(project).azureDevOps.definitions
	}

	@Override
	String getBuildUrl(IDefinition definition, IBuild build) {
		return String.format("%s%s/%s/_build/results?buildId=%s", AzureDevOpsClient.URL,
			definition.organization, definition.project, build.id)
	}
}