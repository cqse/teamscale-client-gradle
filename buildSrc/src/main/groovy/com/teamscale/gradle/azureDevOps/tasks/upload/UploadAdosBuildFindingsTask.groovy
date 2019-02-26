package com.teamscale.gradle.azureDevOps.tasks.upload

import com.teamscale.gradle.azureDevOps.data.AdosBuild
import com.teamscale.gradle.azureDevOps.data.AdosDefinition
import com.teamscale.gradle.azureDevOps.tasks.base.UploadBuildFindingsTask
import com.teamscale.gradle.azureDevOps.utils.loganalyzer.LogAnalyzerFactory
import com.teamscale.gradle.teamscale.data.TeamscaleExtension
import com.teamscale.gradle.teamscale.data.TeamscaleFinding

import static com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils.log

class UploadAdosBuildFindingsTask extends UploadBuildFindingsTask<AdosDefinition, AdosBuild> {
	static final String TASK_NAME = "uploadBuildFindings"

	/** Maximum number of lines of a log which will be downloaded at once */
	static final Integer MAX_LOG_LINES = 10000

	@Override
	void run(AdosDefinition definition, AdosBuild build) {
		List logs = definition.http.getTimelineOfBuild(build.id)
		List matchedLogs = getMatchingLogs(definition, build, logs)

		if (matchedLogs.isEmpty()) {
			log("No log(s) matched $definition.options.logNameMatcher. Remember that the regex" +
				"must match the complete name, not just a part! The name of all available logs are: ${logs.name}",
				definition, build)
			return
		}

		Set<TeamscaleFinding> findings = parseFindingsFromLogs(matchedLogs, definition, build)

		upload(definition, build, findings)
	}

	/** Downloads the content of the given logs and extracts possible findings from them */
	static Set<TeamscaleFinding> parseFindingsFromLogs(List<Object> logs, AdosDefinition definition, AdosBuild build) {
		Set<TeamscaleFinding> findings = new HashSet<>()

		logs.each { log ->
			def currentLine = 0
			while (currentLine < log.lineCount) {
				def endLine = currentLine + MAX_LOG_LINES
				String logsContent = definition.http.downloadLog(build.id, "$log.id", currentLine, endLine)

				def logAnalyzer = LogAnalyzerFactory.getFor(definition.options.logNameMatcher.type)
				findings.addAll(parseLog(logsContent, logAnalyzer))

				// endLine is inclusive. Add one in order to prevent parsing a line twice
				currentLine += MAX_LOG_LINES + 1
			}
		}

		return findings
	}

	/** Returns a list of all logs of the build which matched the "logNamePattern" */
	static List getMatchingLogs(AdosDefinition definition, AdosBuild build, List logs) {
		def matchingLogs = logs.findAll {
			def nameMatches = definition.options.logNameMatcher.pathMatches(it.name)
			def hasLog = (it.log != null)

			return hasLog && nameMatches
		}

		// get the logs with the line count
		def allLogDescriptions = definition.http.getLogsOfBuild(build.id).groupBy {
			it.id
		}

		return matchingLogs.collect {
			[
				id       : it.log.id,
				lineCount: allLogDescriptions[it.log.id][0].lineCount
			]
		}
	}

	@Override
	boolean isConfiguredForTask(AdosDefinition definition) {
		return definition.options.logNameMatcher != null
	}

	@Override
	List<AdosDefinition> getDefinitions() {
		return TeamscaleExtension.getFrom(project).azureDevOps.definitions
	}
}