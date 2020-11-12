package com.teamscale.gradle.munichre.tasks


import com.teamscale.gradle.munichre.extensions.DashboardExtension
import com.teamscale.gradle.teamscale.TeamscaleClient
import com.teamscale.gradle.teamscale.data.TeamscaleExtension
import com.teamscale.gradle.teamscale.tasks.dashboard.DashboardUtils
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.util.slurpersupport.NodeChild
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils.log
import static com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils.warn

/**
 * Tasks for creating dashboards for all projects defined in the "portfolio dashboard" data based on a template
 */
class CreateUniformDashboards extends DefaultTask {

	protected static Map<Integer, String> QUALITY_GOAL_DESCRIPTIONS = [
		0: "TBD",
		1: "indifferent",
		2: "preserving",
		3: "improving",
		4: "perfective"
	]

	public static final String TASK_NAME = "createUniformDashboards"

	@TaskAction
	def action() {
		TeamscaleClient http = TeamscaleExtension.getFrom(project).http
		DashboardExtension config = project.extensions.getByName(DashboardExtension.NAME) as DashboardExtension

		if (!checkConfig(config)) {
			return
		}


		String template = DashboardUtils.getDashboard(http, config.template)
		Map data = new JsonSlurper().parse(new File(config.data)) as Map

		Set<String> projects = http.getAllProjects();
		for (ProjectInfo info in getProjectInfos(data, config.tool)) {
			if (config.projectMustExist && !projects.contains(info.id)) {
				warn(String.format("No project in teamscale found for %s", info.id));
			} else {
				def dashboard = createDashboard(info, template, config)
				log(String.format("Creating dashboard for %s: %s", info.id, dashboard.getName()))
				DashboardUtils.uploadDashboard(http, dashboard)
			}
		}
	}

	/**
	 * Create the dashboard by replacing values in a template dashboard
	 */
	private Dashboard createDashboard(ProjectInfo info, String template, DashboardExtension config) {
		// parse the XML
		Object templateJSON = new JsonSlurper().parseText(template)

		// change XML
		templateJSON.name = info.dashboardName

		// Set sharing
		setSharing(templateJSON, info, config.placeholder)

		// change JSON
		Object json = new JsonSlurper().parseText(templateJSON.descriptorJSON.text())
		json.widgets.each { widget ->
			// Label Title
			if (widget["widget-id"] == "label") {
				widget.Title = widget.Title
					.replaceFirst(/<[^>]+>/, info.qualifiedName)
					.replace("<QG>", info.qualityGoal as String)
					.replace("<DESCRIPTION>", QUALITY_GOAL_DESCRIPTIONS[info.qualityGoal])
				return
			}

			// Project paths
			if (widget.Path) {
				widget.Path.project = info.id
				widget.Path.path = info.path
			}

			// Baselines
			if (widget.Trend && widget.Trend.type == "BASELINE") {
				widget.Trend.value.project = info.id
			}
		}
		templateJSON.descriptorJSON = new JsonBuilder(json).toPrettyString()

		return new Dashboard(templateJSON)
	}

	/** Sets the sharing for the given dashboard */
	static def setSharing(dashboard, ProjectInfo projectInfo, String placeholder) {
		dashboard.projectAccessEntries.each { child ->
			if (child.userOrGroup == placeholder) {
				child.userOrGroup = projectInfo.id
			}
		}
	}

	/** Assemble the information of all projects which have a dashboard configured */
	List<ProjectInfo> getProjectInfos(Map<String, Map> data, String toolName) {
		List<ProjectInfo> projectInfos = new ArrayList<>()
		for (entrySet in data.entrySet()) {
			Map value = entrySet.value

			ProjectInfo info = new ProjectInfo()

			info.qualifiedName = value.name
			def tool = value[toolName]
			if (!tool) {
				continue
			}

			def teamscale = tool.teamscale
			if (!(teamscale && teamscale.project)) {
				// If the tool is not in teamscale we don't need to create a dashboard
				continue
			}

			info.id = teamscale.project

			info.dashboardName = teamscale.dashboard
			if (!info.dashboardName) {
				info(String.format("Teamscale project, but no dashboard configured for %s %s", toolName, info))
				continue
			}
			info.dashboardName = info.dashboardName.split("admin/").last()


			info.path = teamscale.path ? teamscale.path : ""
			info.qualityGoal = tool?.qualityGoal ? tool.qualityGoal : 0

			projectInfos.add(info)
		}
		return projectInfos
	}

	/** Check that everything necessary for the job is configured.  */
	boolean checkConfig(DashboardExtension config) {
		if (!config) {
			log("No dashboard info configured for this task")
			return false
		}

		TeamscaleExtension teamscale = TeamscaleExtension.getFrom(project)
		if (!teamscale?.config?.url) {
			warn("No server configured!")
			return false
		}

		if (!config.template) {
			warn("No template configured")
			return false
		}

		if (!config.data && !(new File(config.data).exists())) {
			warn("No valid properties path given")
			return false
		}

		if (!DashboardUtils.dashboardExists(teamscale.http, config.template)) {
			warn("The dashboard template $config.template does not exist")
			return false
		}

		return true
	}

	class ProjectInfo {
		/** project id */
		String id

		/** additional path inside of the project */
		String path

		/** Printable Name of the project */
		String qualifiedName

		/** Name of the dashboard */
		String dashboardName

		int qualityGoal

		String toString() {
			return String.format("[id:%s, path:%s, qualifiedName:%s]", id, path, qualifiedName)
		}
	}

	/** Wrapper for the Dashboard JSON */
	class Dashboard {
		NodeChild json

		Dashboard(json) {
			this.json = json
		}

		String getName() {
			return this.json.owner + "/" + this.json.name
		}

		String toString() {
			return new JsonBuilder(this.json).toString()
		}
	}
}