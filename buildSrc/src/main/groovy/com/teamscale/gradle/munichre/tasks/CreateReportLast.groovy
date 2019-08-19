package com.teamscale.gradle.munichre.tasks

class CreateReportLast extends CreateBaselineIfMissing {
	public static final String TASK_NAME = "CreateReportLastIfMissing"

	@Override
	String getBaselineName() {
		return "Report Last"
	}
}
