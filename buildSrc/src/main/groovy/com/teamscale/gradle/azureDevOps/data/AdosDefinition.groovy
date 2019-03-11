package com.teamscale.gradle.azureDevOps.data

import com.teamscale.gradle.azureDevOps.client.AzureDevOpsClient
import com.teamscale.gradle.azureDevOps.extensions.BuildDefinitionConfiguration
import com.teamscale.gradle.azureDevOps.utils.Cache
import com.teamscale.gradle.azureDevOps.tasks.EBuildInformationType

import java.time.Instant

import static com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils.log

class AdosDefinition implements IDefinition {
	/** Options for this definition */
	final BuildDefinitionConfiguration options

	final String name

	final String id

	final Instant lastCompletedTime

	/** Reference to the project cache */
	final Cache cache

	final AzureDevOpsClient http

	final List<AdosBuild> builds = new ArrayList<>()

	AdosDefinition(BuildDefinitionConfiguration options, AzureDevOpsClient http, Object data, Cache cache) {
		this.options = options
		this.name = data.name
		this.id = data.id
		this.http = http
		this.cache = cache

		def time = null
		if (!data.latestCompletedBuild) {
			log("No build run/completed for $name")
		} else {
			time = Instant.parse((String) data.latestCompletedBuild.finishTime)
		}
		lastCompletedTime = time
	}

	@Override
	String getOrganization() {
		return http.organization
	}

	@Override
	String getProject() {
		return http.project
	}

	/**
	 * Returns the smallest time a build was processed for all of the given types it can be processed for.
	 * If no type is given or if no build has been processed yet for the given types Instant.EPOCH is returned
	 */
	Instant getMinLastProcessedTimeFor(List<EBuildInformationType> types) {
		return cache.getMinTime(this, types)
	}

	@Override
	String getPartition() {
		return options.partition
	}
}