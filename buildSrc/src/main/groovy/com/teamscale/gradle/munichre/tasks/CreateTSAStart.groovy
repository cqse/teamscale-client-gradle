package com.teamscale.gradle.munichre.tasks

class CreateTSAStart extends CreateBaselineIfMissing {
	public static final String TASK_NAME = "CreateTSAStartIfMissing"

	@Override
	String getBaselineName() {
		return "TSA Start"
	}
}
