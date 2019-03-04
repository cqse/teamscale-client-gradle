package com.teamscale.gradle.azureDevOps.data

import com.teamscale.gradle.azureDevOps.tasks.base.EBuildResult

import java.time.Instant

/**
 * Information of a specific build from ADOS.
 */
class AdosBuild implements IBuild {
	String id
	String buildNumber
	EBuildResult result
	Instant startTime
	Instant finishTime
	Instant queueTime
	String sourceBranch
	String targetBranch
	String uri

	Instant latestProcessedReleaseTest = Instant.EPOCH

	AdosBuild(Map data, Closure branchMapping) {
		// Maps the json properties to the actual object fields
		metaClass.properties.findAll { it.name != "class" && data.containsKey(it.name) }.each {
			Object value
			if (it.type.equals(Instant)) {
				value = Instant.parse((String) data[it.name])
			} else if (it.type.equals(EBuildResult)) {
				value = EBuildResult.fromString((String) data[it.name])
			} else {
				// default is string
				value = data[it.name]
			}

			it.setProperty(this, value)
		}

		setTargetBranch(branchMapping)
	}

	/**
	 * Sets the target branch of the build, meaning the branch it will be uploaded to in teamscale.
	 * If no branchMapping is set it will be uploaded to the default branch (main).
	 */
	private void setTargetBranch(Closure branchMapping) {
		if (shouldBeExcluded(sourceBranch)) {
			targetBranch = null
			return
		}

		sourceBranch = formatBranchName(sourceBranch)
		targetBranch = branchMapping(sourceBranch)

		if (targetBranch && targetBranch.startsWith("\$")) {
			// A branch name starting with $ is a TFS path and not a branch and
			// should always be mapped to main if not stated otherwise.
			targetBranch = ""
		}
	}

	/** Whether the build failed */
	boolean hasFailed() {
		return this.result == EBuildResult.FAILED
	}

	/**
	 * Checks if the branch name suggest that this might be, for example, a pull
	 * request, which should than automatically be excluded from the processing.
	 */
	private static boolean shouldBeExcluded(String branchName) {
		return branchName =~ /^refs\/pull/
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
	@Override
	long getExecutionTime() {
		return (finishTime.toEpochMilli() - startTime.toEpochMilli()) / 1000
	}

	/** Return the time the build finished */
	@Override
	Instant getFinishTime() {
		return finishTime
	}

	void setLatestProcessedReleaseTest(Instant latestProcessedReleaseTest) {
		this.latestProcessedReleaseTest = latestProcessedReleaseTest
	}

	Instant getLatestProcessedReleaseTest() {
		return latestProcessedReleaseTest
	}

	@Override
	Instant getStartTime() {
		return queueTime
	}

	@Override
	String getTeamscaleBranch() {
		return targetBranch
	}

	@Override
	String getName() {
		return buildNumber
	}
}