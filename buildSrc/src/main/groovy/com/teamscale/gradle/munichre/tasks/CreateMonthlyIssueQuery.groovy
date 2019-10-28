package com.teamscale.gradle.munichre.tasks

import com.teamscale.gradle.teamscale.TeamscaleClient
import com.teamscale.gradle.teamscale.data.TeamscaleExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import static com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils.log

class CreateMonthlyIssueQuery extends DefaultTask {
	public static final String TASK_NAME = "createMonthlyIssueQuery"

	/**
	 * Maximum number of issue queries for the monthly assessment.
	 * Every additional query will be deleted
	 */
	private static final Integer MAX_QUERIES = 4

	/**
	 * The issue query for the monthly assessment, for issues closed between two dates.
	 * String can be formatted with two parameters, the first being the start date and the second the end date.
	 */
	private static final String ISSUE_QUERY_CLOSED_BETWEEN = "" +
		"('Work Item Type' != Task and closed=true and inState(closed=true) >= %1\$s and inState(closed=true) < %2\$s) or " +
		"('Work Item Type' = Task and hasParent(closed=true and inState(closed=true) >= %1\$s and inState(closed=true) < %2\$s)) or " +
		"('Work Item Type' = Task and parent = '' and closed=true and inState(closed=true) >= %1\$s and inState(closed=true) < %2\$s)"

	/** The format for the name of the issue query */
	private static final String ISSUE_QUERY_NAME = "Monthly Assessment %s"

	/** Name of the issue query for the current monthly assessment */
	private static final String MONTHLY_CURRENT = "Monthly Assessment (Current)"

	/** Name of the issue query for the next monthly assessment */
	public static final String MONTHLY_NEXT = "Monthly Assessment (Next)"

	private static final DateTimeFormatter NAME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM")

	private static final DateTimeFormatter QUERY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd")

	@TaskAction
	def action() {
		TeamscaleClient http = TeamscaleExtension.getFrom(project).http

		for (project in http.getAllProjects()) {
			if (hasNoIssues(http, project)) {
				log("Skipping $project: no issues found")
				continue
			}

			log("Creating monthly issue metrics for $project")

			// get current issue metrics
			List<Object> queries = getIssueQueries(http, project)

			// delete old
			queries.forEach { query ->
				if (query.name.startsWith("Monthly Assessment")) {
					deleteIssueQuery(http, project, (String) query.name)
				}
			}

			// create new issue metrics
			LocalDate next = LocalDate.now().withDayOfMonth(1)
			for (int i = 0; i < MAX_QUERIES; i++) {
				LocalDate start = next.minusMonths(i)
				uploadIssueQuery(http, project, getIssueQueryName(start, i), getIssueQuery(start))
			}
		}
	}

	/** Fetches all monthly assessment issue queries for the given project */
	static List<Object> getIssueQueries(TeamscaleClient http, String project) {
		List<Object> allQueries = http.doCall("get", ["p", project, "issue-metrics"], ["detail": "true"], { request ->
			request.accept = "application/json"
		}) as List<Object>

		return allQueries.findResults {
			if (((String) it.name).startsWith("Monthly Assessment")) {
				return it
			}
		}
	}

	/** Deletes the issue query of a project with the given name */
	static void deleteIssueQuery(TeamscaleClient http, String project, String name) {
		http.doCall("delete", ["p", project, "issue-metrics", name])
	}

	/** Uploads the given issue query to teamscale */
	static void uploadIssueQuery(TeamscaleClient http, String project, String issueQueryName, String query) {
		def setBody = { request ->
			request.contentType = "application/json"
			request.body = ["name": issueQueryName, "query": query]
		}

		http.doCall("put", ["p", project, "issue-metrics", issueQueryName], [:], setBody)
	}

	/** Creates the issue query for the given start and end date */
	static String getIssueQuery(LocalDate start) {
		LocalDate end = start.withMonth(start.plusMonths(1).monthValue)

		return String.format(ISSUE_QUERY_CLOSED_BETWEEN, start.format(QUERY_FORMAT), end.format(QUERY_FORMAT))
	}

	/** Returns the name of the issue query for the given start date */
	static String getIssueQueryName(LocalDate startDate, int number) {
		if (number == 0) {
			return MONTHLY_NEXT
		}

		if (number == 1) {
			return MONTHLY_CURRENT
		}

		return String.format(ISSUE_QUERY_NAME, startDate.format(NAME_FORMAT))
	}

	/**
	 * Checks if the given project has any issues by querying the number of issues
	 */
	static boolean hasNoIssues(TeamscaleClient http, String project) {
		return (http.doCall("get", ["p", project, "issues"], ["count": "true"], { request ->
			request.accept = "application/json"
		}) as int) == 0
	}
}
