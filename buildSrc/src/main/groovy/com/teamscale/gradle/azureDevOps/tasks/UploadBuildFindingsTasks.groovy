package com.teamscale.gradle.azureDevOps.tasks


import com.teamscale.gradle.azureDevOps.data.Build
import com.teamscale.gradle.azureDevOps.data.Definition
import com.teamscale.gradle.teamscale.TeamscaleClient
import com.teamscale.gradle.teamscale.TeamscaleExtension
import com.teamscale.gradle.teamscale.TeamscaleFinding

import static com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils.log
import static com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils.warn

class UploadBuildFindingsTasks extends UploadTask {
	static final String NAME = "uploadBuildFindings"

	/** Maximum number of lines of a log which will be downloaded at once */
	static final Integer MAX_LOG_LINES = 10000

	@Override
	EBuildInformationType getUploadType() {
		EBuildInformationType.BUILD_FINDINGS
	}

	@Override
	void run(Definition definition, Build build) {
		List logs = getMatchingLogs(definition, build)

		if (logs.size() == 0) {
			log("No log(s) matched '$definition.options.logNamePattern'. " +
				"The names of the logs are ${logs.name}", definition, build)
			return
		}

		Set<TeamscaleFinding> findings = parseFindingsFromLogs(logs, definition, build)

		// Upload findings
		TeamscaleClient http = TeamscaleExtension.getFrom(project).http

		def params = getStandardQueryParameters(EUploadPartitionType.METRICS, definition, build)
		def result = http.uploadExternalFindings(params, new ArrayList<>(findings))

		if (result == TeamscaleClient.UPLOAD_SUCCESS_RETURN) {
			log("Uploading ${findings.size()} finding(s): $result", definition, build)
			definition.setLastProcessedTime(getUploadType(), build)
		} else {
			warn("Upload was not successful: $result", definition, build)
		}
	}

	/**
	 * Downloads the content of the given logs and extracts possible findings from them
	 */
	Set<TeamscaleFinding> parseFindingsFromLogs(List<Object> logs, Definition definition, Build build) {
		Set<TeamscaleFinding> findings = new HashSet<>()

		logs.each { log ->
			def currentLine = 0
			while (currentLine < log.lineCount) {
				def endLine = currentLine + MAX_LOG_LINES
				String logsContent = definition.http.downloadLog(build.id, "$log.id", currentLine, endLine)

				findings.addAll(parseLog(logsContent))

				// endLine is inclusive. Add one in order to prevent parsing a line twice
				currentLine += MAX_LOG_LINES + 1
			}
		}

		return findings
	}

	/**
	 * Parses any findings from the given string
	 */
	Set<TeamscaleFinding> parseLog(String content) {
		Set<TeamscaleFinding> findings = []
		content.eachLine { String line ->
			TeamscaleFinding finding = TeamscaleExtension.getFrom(project).azureDevOps.logAnalyzer.analyze(line)
			if (finding) {
				findings.add(finding)
			}
		}
		return findings
	}

	/**
	 * Returns a list of all logs of the build which matched the "logNamePattern"
	 */
	static List getMatchingLogs(Definition definition, Build build) {
		def logs = definition.http.getTimelineOfBuild(build.id).records

		def matchingLogs = logs.findAll {
			def nameMatches = it.name ==~ definition.options.logNamePattern
			def noErrors = it.state == "completed" && it.result == "succeeded"
			def hasLog = (it.log != null)
			return hasLog && nameMatches && noErrors
		}

		// get the logs with the linecount
		def allLogDescriptions = definition.http.getLogsOfBuild(build.id).value.groupBy {
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
	boolean isConfiguredForTask(Definition definition) {
		def noPattern = definition.options.logNamePattern == null
		def noLogAnalyzer = TeamscaleExtension.getFrom(project).azureDevOps.logAnalyzer == null

		if (noLogAnalyzer) {
			log("No log analyzer defined", definition)
			return false
		}

		if (noPattern) {
			log("No name pattern for choosing a log found", definition)
			return false
		}

		return true
	}
}