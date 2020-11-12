package com.teamscale.gradle.munichre.tasks

import com.teamscale.gradle.teamscale.TeamscaleClient

/**
 * Task for creating monthly baselines for a project
 */
class CreateMonthlyBaselinesOld extends CreateBaselineOld {
	public static final String TASK_NAME = "createMonthlyBaselinesOld"
	private static final String CURRENT = "This Month"
	private static final String PREVIOUS = "Previous Month"

	@Override
	void execute(TeamscaleClient http, String project) {
		Date baseline = getDateAtStartOfCurrentMonth()
		createBaseline(project, CURRENT, baseline.getTime())

		baseline.month--
		createBaseline(project, PREVIOUS, baseline.getTime())
	}

	/**
	 * Returns a date without a time, at the start of the current month
	 */
	static Date getDateAtStartOfCurrentMonth() {
		def date = new Date()
		date.clearTime()
		date.date = 1
		return date
	}
}
