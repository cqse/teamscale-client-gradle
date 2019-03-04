package com.teamscale.gradle.azureDevOps.data

import com.teamscale.gradle.azureDevOps.tasks.base.EBuildResult

import java.time.Instant

interface IBuild {
	Instant getStartTime()

	Instant getFinishTime()

	long getExecutionTime()

	String getTeamscaleBranch()

	String getName()

	EBuildResult getResult()

	boolean hasFailed()
}
