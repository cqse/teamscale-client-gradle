package com.teamscale.gradle.munichre.tasks

import com.teamscale.gradle.teamscale.TeamscaleClient
import com.teamscale.gradle.teamscale.data.TeamscaleExtension
import groovy.json.JsonBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils.log

/**
 * Task for automatically creating baselines for all projects.
 */
abstract class CreateBaseline extends DefaultTask {
	public static final String TASK_NAME = "createBaselines"

	private TeamscaleClient http = TeamscaleExtension.getFrom(project).http

	@TaskAction
	def action() {
		for (project in http.getAllProjects()) {
			execute(http, project)
		}
	}

	abstract void execute(TeamscaleClient http, String project)

	/** Create the baseline for the given project */
	protected void createBaseline(String project, String name, long baseline) {
		log("Creating \"$name\" for $project")
		http.doCall("put", ["api", "projects", project, "baselines", name], [:], { request ->
			request.contentType = "application/json"
			request.body = new JsonBuilder([
				"description": "",
				"name"       : name,
				"timestamp"  : baseline
			]).toPrettyString()
		})
	}

	/** Returns the timestamp of the first commit */
	long getTimeStampOfFirstCommit(String project) {
		Object summary = http.doCall("get", ["api", "projects", project, "repository-summary"], [:], { request ->
			request.accept = "application/json"
		}) as Object

		return summary.firstCommit as long
	}

	/** Checks if the project has a baseline with the same name */
	boolean hasBaseline(String project, String baseline) {
		return (http.doCall("get", ["api", "projects", project, "baselines"], [:], { request ->
			request.accept = "application/json"
		}) as List<String>).contains(baseline)
	}
}
