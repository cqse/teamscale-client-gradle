package com.teamscale.gradle.munichre.tasks

class CreateReportLastOld extends CreateBaselineIfMissingOld {
	public static final String TASK_NAME = "createReportLastIfMissingOld"

	@Override
	String getBaselineName() {
		return "Report Last"
	}
}
