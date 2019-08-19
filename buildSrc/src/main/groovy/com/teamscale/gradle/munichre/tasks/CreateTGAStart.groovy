package com.teamscale.gradle.munichre.tasks

class CreateTGAStart extends CreateBaselineIfMissing {
	public static final String TASK_NAME = "CreateTGAStartIfMissing"

	@Override
	String getBaselineName() {
		return "TGA Start"
	}

	@Override
	long getBaseline(String project) {
		// set baseline one minute after first commit
		return getTimeStampOfFirstCommit(project) + (1000l * 60)
	}
}
