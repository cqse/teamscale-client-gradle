package com.teamscale.gradle.munichre.tasks

import com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils
import com.teamscale.gradle.munichre.extensions.DashboardExtension
import com.teamscale.gradle.teamscale.TeamscaleClient
import com.teamscale.gradle.teamscale.data.TeamscaleExtension
import com.teamscale.gradle.teamscale.tasks.dashboard.DashboardUtils
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.util.slurpersupport.GPathResult
import groovy.util.slurpersupport.NodeChild
import groovy.xml.XmlUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

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

		for (ProjectInfo info in getProjectInfos(data, config.tool)) {
			def dashboard = createDashboard(info, template)
			info(String.format("Creating dashboard for %s", info.id))
			DashboardUtils.uploadDashboard(http, dashboard)
		}
	}

	/**
	 * Create the dashboard by replacing values in a template dashboard
	 */
	private Dashboard createDashboard(ProjectInfo info, String template) {
		// parse the XML
		GPathResult xml = new XmlSlurper().parseText(template)

		// change XML
		xml.payload.name = info.dashboardName

		// Set sharing
		setSharing(xml, info)

		// change JSON
		Object json = new JsonSlurper().parseText(xml.payload.descriptorJSON.text())
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
		xml.payload.descriptorJSON = new JsonBuilder(json).toPrettyString()

		return new Dashboard(xml)
	}

	/** Sets the sharing for the given dashboard */
	static def setSharing(dashboard, projectInfo) {
		dashboard.payload.projectAccessEntries.children().each { child ->
			if (child.userOrGroup == "dummy") {
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
			info.id = entrySet.key
			info.qualifiedName = value.name
			def tool = value[toolName]
			if (!tool) {
				continue
			}

			def teamscale = tool.teamscale
			if (!teamscale) {
				warn(String.format("No teamscale configured for %s", info.id))
				continue
			}

			info.dashboardName = teamscale.dashboard
			if (!info.dashboardName) {
				info(String.format("No dashboard configured for %s", info.id))
				continue
			}
			info.dashboardName = URLDecoder.decode(info.dashboardName.split("%2F").last(), "utf-8")

			info.path = teamscale.path ? teamscale.path : ""
			if (info.path != "") {
				// Get actual teamscale path and not the link to the code metric with the path
				List<String> parts = path.split("/") as List
				info.path = parts.subList(parts.indexOf("code.html#") + 2, parts.size()).join("/")
			}

			info.qualityGoal = tool?.qualityGoal ? tool.qualityGoal : 0

			projectInfos.add(info)
		}
		return projectInfos
	}

	/** Check that everything necessary for the job is configured.  */
	boolean checkConfig(DashboardExtension config) {
		if (!config) {
			LoggingUtils.log("No dashboard info configured for this task")
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

		if (!DashboardUtils.getAllDashboards(teamscale.http).contains(config.template)) {
			warn("The dashboard template $config.template does not exist")
			return false
		}

		return true
	}

	class ProjectInfo {
		String id
		String path
		String qualifiedName
		String dashboardName
		int qualityGoal
	}

	/** Wrapper for the Dashboard XML */
	class Dashboard {
		NodeChild xml

		Dashboard(xml) {
			this.xml = xml
		}

		String getName() {
			return this.xml.payload.owner + "/" + this.xml.payload.name
		}

		String toString() {
			return XmlUtil.serialize(this.xml)
		}
	}
}