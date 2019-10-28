package com.teamscale.gradle.munichre.tasks

import com.teamscale.gradle.munichre.extensions.IssueQueryExtension
import com.teamscale.gradle.teamscale.TeamscaleClient
import com.teamscale.gradle.teamscale.data.TeamscaleExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils.log

class CreateIssueQueries extends DefaultTask {
	public static final String TASK_NAME = "createIssueQueries"

	@TaskAction
	def action() {
		IssueQueryExtension issueConfig = project.extensions.getByName(IssueQueryExtension.NAME) as IssueQueryExtension

		if (issueConfig.queries.size() == 0) {
			return
		}

		TeamscaleClient http = TeamscaleExtension.getFrom(project).http

		for (project in http.getAllProjects()) {
			if (CreateMonthlyIssueQuery.hasNoIssues(http, project)) {
				log("Skipping $project: no issues found")
				continue
			}

			issueConfig.queries.forEach { queryName, query ->
				log("Creating standard issue queries for '$project'")
				CreateMonthlyIssueQuery.uploadIssueQuery(http, project, queryName, query)
			}
		}

	}
}
