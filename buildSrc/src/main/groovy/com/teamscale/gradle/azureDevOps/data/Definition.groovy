package com.teamscale.gradle.azureDevOps.data

import com.teamscale.gradle.azureDevOps.client.AzureDevOpsClient
import com.teamscale.gradle.azureDevOps.config.BuildOptions
import com.teamscale.gradle.azureDevOps.config.EBuildInformationType
import com.teamscale.gradle.azureDevOps.tasks.Cache

import java.time.Instant

import static com.teamscale.gradle.azureDevOps.utils.Logging.log

class Definition {
	/** Options for this definition */
	final BuildOptions options

	final String name

	final String id

	final Instant lastCompletedTime

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

	String getOrganization() {
		return http.organization
	}

	String getProject() {
		return http.project
	}

	void setLastProcessedTime(EBuildInformationType option, Build build) {
		cache.set(this, option, build)
	}

	Instant getMinLastProcessedTime() {
		return cache.getMinTime(this)
	}
}