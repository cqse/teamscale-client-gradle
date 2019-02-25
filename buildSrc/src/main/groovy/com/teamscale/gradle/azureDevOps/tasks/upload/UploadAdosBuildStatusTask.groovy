package com.teamscale.gradle.azureDevOps.tasks.upload

import com.teamscale.gradle.azureDevOps.data.AdosBuild
import com.teamscale.gradle.azureDevOps.data.AdosDefinition
import com.teamscale.gradle.azureDevOps.tasks.base.UploadBuildStatusTask
import com.teamscale.gradle.teamscale.data.TeamscaleExtension

class UploadAdosBuildStatusTask extends UploadBuildStatusTask<AdosDefinition, AdosBuild> {
	public final static String TASK_NAME = "uploadAdosBuildStatus"

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
	protected boolean canBeProcessed(AdosDefinition definition, AdosBuild build) {
		return true
	}

	@Override
	List<AdosDefinition> getDefinitions() {
		return TeamscaleExtension.getFrom(project).azureDevOps.definitions
	}
}
