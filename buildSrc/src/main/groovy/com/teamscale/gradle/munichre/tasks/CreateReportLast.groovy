package com.teamscale.gradle.munichre.tasks

class CreateReportLast extends CreateBaselineIfMissing {
	public static final String TASK_NAME = "createReportLastIfMissing"

	@Override
	String getBaselineName() {
		return "Report Last"
	}
}
