package com.teamscale.gradle.azureDevOps.data

import com.teamscale.gradle.azureDevOps.client.AzureDevOpsClient
import com.teamscale.gradle.azureDevOps.config.BuildOptions
import com.teamscale.gradle.azureDevOps.tasks.EBuildInformationType
import com.teamscale.gradle.azureDevOps.tasks.Cache

import java.time.Instant

import static com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils.log

class Definition {
	/** Options for this definition */
	final BuildOptions options

	final String name

	final String id

	final Instant lastCompletedTime

	/** Reference to the project cache */
	final Cache cache

	final AzureDevOpsClient http

	final List<Build> builds = new ArrayList<>()

	Definition(BuildOptions options, AzureDevOpsClient http, Map data, Cache cache) {
		this.options = options
		this.name = data.name
		this.id = data.id
		this.http = http
		this.cache = cache

		def time = Instant.EPOCH
		if (!data.latestCompletedBuild) {
			log("No build run/completed for $name")
		} else {
			time = Instant.parse(data.latestCompletedBuild.finishTime)
		}
		lastCompletedTime = time
	}

	/**
	 * Return the ADOS organization where the definition is located.
	 * E.g. 'apps-munichre'
	 */
	String getOrganization() {
		return http.organization
	}

	/**
	 * Return the project which contains the definition.
	 */
	String getProject() {
		return http.project
	}

	void setLastProcessedTime(EBuildInformationType option, Build build) {
		cache.set(this, option, build)
	}

	/**
	 * Returns the smallest time a build was processed for all of the given types it can be processed for.
	 * If no type is given or if no build has been processed yet for the given types Instant.EPOCH is returned
	 */
	Instant getMinLastProcessedTimeFor(List<EBuildInformationType> types) {
		return cache.getMinTime(this, types)
	}
}