package com.teamscale.gradle.azureDevOps.data

import com.teamscale.gradle.azureDevOps.utils.Cache

interface IDefinition {
	String getName()

	String getPartition()

	String getProject()

	String getOrganization()

	Cache getCache()

	List<IBuild> getBuilds()
}