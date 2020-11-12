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
abstract class CreateBaselineOld extends DefaultTask {
	public static final String TASK_NAME = "createBaselinesOld"

	private TeamscaleClient http = TeamscaleExtension.getFrom(project).http

	@TaskAction
	def action() {
		for (project in http.getAllProjects()) {
			execute(http, project)
		}
	}

	abstract void execute(TeamscaleClient http, String project)

	/**
	 * Create the baseline for the given project
	 */
	protected void createBaseline(String project, String name, long baseline) {
		log("Creating \"$name\" for $project")
		http.doProjectCallOld("put", ["baselines", name], [:], { request ->
			request.contentType = "application/json"
			request.body = new JsonBuilder([
				"description": "",
				"name"       : name,
				"timestamp"  : baseline
			]).toPrettyString()
		})
	}

	/**
	 * Returns the timestamp of the first commit
	 */
	long getTimeStampOfFirstCommit(String project) {
		Object summary = http.doProjectCallOld("get", "repository-summary", [:], { request ->
			request.accept = "application/json"
		}) as Object

		return summary.firstCommit as long
	}

	/** Checks if the project has a baseline with the same name */
	boolean hasBaseline(String project, String baseline) {
		return (http.doProjectCallOld("get", "baselines", [:], { request ->
			request.accept = "application/json"
		}) as List<String>).contains(baseline)
	}
}
