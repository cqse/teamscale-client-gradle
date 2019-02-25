package com.teamscale.gradle.azureDevOps.tasks.base

import com.teamscale.gradle.azureDevOps.data.AdosBuild
import com.teamscale.gradle.azureDevOps.data.AdosDefinition
import com.teamscale.gradle.teamscale.data.TeamscaleExtension

/**
 * Superclass for tasks which are based on the processing of a single build
 */
abstract class AdosUploadTask extends UploadTask<AdosDefinition, AdosBuild> {
	public final static String TASK_GROUP = "Azure Build Information Upload"

	@Override
	List<AdosDefinition> getDefinitions() {
		return TeamscaleExtension.getFrom(project).azureDevOps.definitions
	}
}