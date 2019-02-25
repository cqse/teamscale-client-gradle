package com.teamscale.gradle.azureDevOps.data

import java.time.Instant

interface IBuild {
	Instant getStartTime()

	Instant getFinishTime()

	long getExecutionTime()

	String getTeamscaleBranch()

	String getName()

	String getResult()

	boolean hasFailed()
}
