package com.teamscale.gradle.azureDevOps.tasks

import com.teamscale.gradle.azureDevOps.data.Build
import com.teamscale.gradle.azureDevOps.data.Definition
import com.teamscale.gradle.teamscale.TeamscaleClient
import com.teamscale.gradle.teamscale.TeamscaleExtension
import com.teamscale.gradle.teamscale.TeamscaleFinding

import static com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils.log

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
		List logs = definition.http.getTimelineOfBuild(build.id)
		List matchedLogs = getMatchingLogs(definition, build, logs)

		if (matchedLogs.isEmpty()) {
			log("No log(s) matched '$definition.options.logNamePattern'. Remember that the regex" +
				"must match the complete name, not just a part! The name of all available logs are: ${logs.name}",
				definition, build)
			return
		}

		Set<TeamscaleFinding> findings = parseFindingsFromLogs(matchedLogs, definition, build)

		// Upload findings
		def params = getStandardQueryParameters(EUploadPartitionType.BUILD_FINDINGS, definition, build)

		TeamscaleClient http = TeamscaleExtension.getFrom(project).http
		def result = http.uploadExternalFindings(params, new ArrayList<>(findings))

		processUploadResult(definition, build, result, "Uploading ${findings.size()} finding(s): $result")
	}

	/**
	 * Downloads the content of the given logs and extracts possible findings from them
	 */
	static Set<TeamscaleFinding> parseFindingsFromLogs(List<Object> logs, Definition definition, Build build) {
		Set<TeamscaleFinding> findings = new HashSet<>()

		logs.each { log ->
			def currentLine = 0
			while (currentLine < log.lineCount) {
				def endLine = currentLine + MAX_LOG_LINES
				String logsContent = definition.http.downloadLog(build.id, "$log.id", currentLine, endLine)

				findings.addAll(parseLog(definition, logsContent))

				// endLine is inclusive. Add one in order to prevent parsing a line twice
				currentLine += MAX_LOG_LINES + 1
			}
		}

		return findings
	}

	/**
	 * Parses any findings from the given string
	 */
	static Set<TeamscaleFinding> parseLog(Definition definition, String content) {
		Set<TeamscaleFinding> findings = []
		content.eachLine { String line ->
			TeamscaleFinding finding = definition.options.logAnalyzer.analyze(line)
			if (finding) {
				findings.add(finding)
			}
		}
		return findings
	}

	/**
	 * Returns a list of all logs of the build which matched the "logNamePattern"
	 */
	static List getMatchingLogs(Definition definition, Build build, List logs) {
		def matchingLogs = logs.findAll {
			def nameMatches = definition.options.logNamePattern.matcher((String) it.name).matches()
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
	boolean isConfiguredForTask(Definition definition) {
		def patternDefined = definition.options.logNamePattern != null
		def logAnalyzerDefined = definition.options.logAnalyzer != null

		return patternDefined && logAnalyzerDefined
	}

	@Override
	String getRejectReason() {
		return "No log analyzer or log name pattern defined"
	}
}