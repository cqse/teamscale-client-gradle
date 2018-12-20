package com.teamscale.gradle.azureDevOps.data

import groovy.json.internal.LazyMap

import java.time.Instant

class Build {
	String id
	String buildNumber
	String result
	Instant startTime
	Instant finishTime
	Instant queueTime
	String sourceBranch
	String targetBranch
	String uri

	Build(LazyMap data, Closure branchMapping) {
		// Maps the json properties to the actual object fields
		metaClass.properties.findAll { it.name != "class" && data.containsKey(it.name) }.each {
			Object value
			if(it.type.equals(String)) {
				value = data[it.name]
			} else if(it.type.equals(Instant)) {
				value = Instant.parse(data[it.name])
			}

			it.setProperty(this, value)
		}

		setTargetBranch(branchMapping)
	}

	private void setTargetBranch(Closure branchMapping) {
		if(shouldBeExcluded(sourceBranch)) {
			targetBranch = null
			return
		}

		sourceBranch = formatBranchName(sourceBranch)
		targetBranch = branchMapping(sourceBranch)

		if(targetBranch && targetBranch.startsWith("\$")) {
			// A branch name starting with $ is a TFS path and not a branch and
			// should always be mapped to main if not stated otherwise.
			targetBranch = ""
		}
	}

	/**
	 * Checks if the branch name suggest that this might be, for example, a pull
	 * request, which should than automatically be excluded from the processing.
	 */
	private static boolean shouldBeExcluded(String branchName) {
		return branchName ==~ /^refs\/pull/
	}

	/**
	 * Some branch name start with misleading prefixes which should be removed
	 * before mapping it to a branch on teamscale.
	 */
	private static String formatBranchName(branchName) {
		def gitRef = ~/^refs\/heads\//
		return branchName - gitRef
	}

	String toString() {
		def props = new HashMap<>(this.properties)
		props.remove("class")
		return props
	}

	/** Returns the time the build took in seconds */
	long getExecutionTime() {
		return (finishTime.toEpochMilli() - startTime.toEpochMilli()) / 1000
	}
}