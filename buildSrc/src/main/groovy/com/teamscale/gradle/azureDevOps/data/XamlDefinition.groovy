package com.teamscale.gradle.azureDevOps.data

import com.teamscale.gradle.azureDevOps.extensions.XamlDefinitionConfiguration
import com.teamscale.gradle.azureDevOps.utils.Cache

import java.nio.file.Path

class XamlDefinition implements IDefinition {

	XamlDefinitionConfiguration config

	String name

	Path buildDir

	List<XamlBuild> builds = []

	Cache cache

	String project

	String organization

	XamlDefinition(XamlDefinitionConfiguration config, Path buildDir, Cache cache, String organization, String project) {
		this.name = config.name
		this.config = config
		this.buildDir = buildDir
		this.cache = cache
		this.project = project
		this.organization = organization
	}

	@Override
	String getName() {
		return name
	}

	@Override
	String getPartition() {
		return config.partition
	}

	@Override
	String getProject() {
		return project
	}

	@Override
	String getOrganization() {
		return organization
	}

	@Override
	Cache getCache() {
		return cache
	}

	@Override
	List<IBuild> getBuilds() {
		return builds
	}
}
