package com.teamscale.gradle.munichre.tasks

import com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils
import com.teamscale.gradle.teamscale.TeamscaleClient

import static com.teamscale.gradle.munichre.tasks.CreateMonthlyIssueQuery.hasNoIssues

/**
 * Task for creating monthly baselines for a project
 */
class CreateMonthlyBaselines extends CreateBaseline {
	public static final String TASK_NAME = "CreateMonthlyBaselines"
	private static final String CURRENT = "This Month"
	private static final String PREVIOUS = "Previous Month"

	@Override
	void execute(TeamscaleClient http, String project) {
		if (hasNoIssues(http, project)) {
			LoggingUtils.log(" Skipping  $project: no issues found")
			return
		}

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
