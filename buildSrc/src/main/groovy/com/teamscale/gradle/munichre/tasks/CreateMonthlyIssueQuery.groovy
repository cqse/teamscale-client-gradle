package com.teamscale.gradle.munichre.tasks

import com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils
import com.teamscale.gradle.teamscale.TeamscaleClient
import com.teamscale.gradle.teamscale.data.TeamscaleExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.regex.Pattern

import static com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils.log

class CreateMonthlyIssueQuery extends DefaultTask {
	public static final String TASK_NAME = "createMonthlyIssueQuery"

	/**
	 * Maximum number of issue queries for the monthly assessment.
	 * Every additional query will be deleted
	 */
	private static final Integer MAX_QUERIES = 3

	/**
	 * The issue query for the monthly assessment
	 * START and END will be replaced with dates
	 */
	private static final String ISSUE_QUERY = "('Work Item Type' != Task and closed=true and inState(closed=true) >= %1\$s and " +
		"inState(closed=true) < %2\$s) or ('Work Item Type' = Task and hasParent(closed=true and " +
		"inState(closed=true) >= %1\$s and inState(closed=true) < %2\$s)) or ('Work Item Type' = Task and " +
		"parent = '' and closed=true and inState(closed=true) >= %1\$s and inState(closed=true) < %2\$s)"

	/** The format for the name of the issue query */
	private static final String ISSUE_QUERY_NAME = "Monthly Assessment %s"

	/** Pattern matching a monthly assessment query with the date as the first group. */
	private static final Pattern ISSUE_QUERY_NAME_PATTERN = Pattern.compile("Monthly Assessment (.*)")

	/** Name of the issue query for the current monthly assessment */
	private static final String ISSUE_QUERY_NAME_CURRENT = "Monthly Assessment"

	private static final DateTimeFormatter NAME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM")

	private static final DateTimeFormatter QUERY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd")

	@TaskAction
	def action() {
		TeamscaleClient http = TeamscaleExtension.getFrom(project).http

		for (project in http.getAllProjects()) {
			log("Creating issue metrics for $project")

			// get current issue metrics
			List<Object> queries = getIssueQueries(http, project)
			LocalDate current = LocalDate.now().minusMonths(1).withDayOfMonth(1)

			// delete current
			def currentQuery = queries.find { it.name == ISSUE_QUERY_NAME_CURRENT }
			if(currentQuery) {
				deleteIssueQuery(http, project, ISSUE_QUERY_NAME_CURRENT)
				queries.remove(currentQuery)
			}

			// delete old
			for(query in queries) {
				String name = query.name
				def matcher = ISSUE_QUERY_NAME_PATTERN.matcher(name)

				if(matcher.matches()) {
					def date
					try {
						date = LocalDate.parse(matcher.group(1)+"-01")
					} catch(DateTimeParseException e) {
						LoggingUtils.warn("Project: $project; the date of the issue query '$name' cannot be parsed")
						continue
					}

					if(date < current.minusMonths(MAX_QUERIES - 1)) {
						deleteIssueQuery(http, project, name)
					}
				}
			}

			// create new issue metrics
			for(int i = 0; i < MAX_QUERIES; i++) {
				uploadIssueQuery(http, project, name, current.minusMonths(i))
			}
		}
	}

	/** Fetches all monthly assessment issue queries for the given project */
	static List<Object> getIssueQueries(TeamscaleClient http, String project) {
		List<Object> allQueries = http.doCall("get", ["p", project, "issue-metrics"], ["detail": "true"], { request ->
			request.accept = "application/json"
		}) as List<Object>

		return allQueries.findResults {
			if(((String) it.name).startsWith("Monthly Assessment")) {
				return it
			}
		}
	}

	/** Deletes the issue query of a project with the given name */
	static void deleteIssueQuery(TeamscaleClient http, String project, String name) {
		http.doCall("delete", ["p", project, "issue-metrics", name])
	}

	/** Uploads the issue query to the project with the give name */
	static void uploadIssueQuery(TeamscaleClient http, String project, String name, LocalDate start) {
		def setBody = { request ->
			request.contentType = "application/json"
			request.body = ["name": getIssueQueryName(start), "query": getIssueQuery(start)]
		}

		http.doCall("put", ["p", project, "issue-metrics", name], [:], setBody)
	}

	/** Creates the issue query for the given start and end date */
	static String getIssueQuery(LocalDate start) {
		LocalDate end = start.withMonth(start.plusMonths(1).monthValue)

		return String.format(ISSUE_QUERY, start.format(QUERY_FORMAT), end.format(QUERY_FORMAT))
	}

	/** Returns the name of the issue query for the given start date */
	static String getIssueQueryName(LocalDate startDate) {
		LocalDate now = LocalDate.now()
		if (startDate.monthValue == (now.minusMonths(1).monthValue) && startDate.year == now.year) {
			return ISSUE_QUERY_NAME_CURRENT
		}
		return String.format(ISSUE_QUERY_NAME, startDate.format(NAME_FORMAT))
	}
}
