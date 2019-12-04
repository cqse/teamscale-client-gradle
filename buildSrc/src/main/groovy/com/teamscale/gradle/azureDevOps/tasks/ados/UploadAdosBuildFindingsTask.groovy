package com.teamscale.gradle.azureDevOps.tasks.ados

import com.teamscale.gradle.azureDevOps.data.AdosBuild
import com.teamscale.gradle.azureDevOps.data.AdosDefinition
import com.teamscale.gradle.azureDevOps.tasks.base.UploadBuildFindingsTask
import com.teamscale.gradle.azureDevOps.utils.AdosBuildLogConfig
import com.teamscale.gradle.azureDevOps.utils.IAdosBuildLogFilter
import com.teamscale.gradle.azureDevOps.utils.ReportLocationMatcher
import com.teamscale.gradle.azureDevOps.utils.loganalyzer.LogAnalyzerFactory
import com.teamscale.gradle.teamscale.data.TeamscaleExtension
import com.teamscale.gradle.teamscale.data.TeamscaleFinding

import static com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils.log
import static com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils.warn

class UploadAdosBuildFindingsTask extends UploadBuildFindingsTask<AdosDefinition, AdosBuild> {
	static final String TASK_NAME = "uploadBuildFindings"

	/** Maximum number of lines of a log which will be downloaded at once */
	static final Integer MAX_LOG_LINES = 10000

	@Override
	void run(AdosDefinition definition, AdosBuild build) {
		List logs = definition.http.getTimelineOfBuild(build.id)

		def configs = definition.options.buildLogConfigs
		if (configs.size() > 1) {
			List<String> partitions = configs.findResults {
				return it.buildLogMatcher.partition
			} as List<String>

			if (configs.size() - 1 > partitions.size()) {
				warn("There are ${configs.size()} parse log configurations, but only ${partitions.size()} partition(s). " +
					"The findings from each parse log must be uploaded to a separate partition in order " +
					"to not overwrite any results. Therefore the number of explicit partitions must be at least " +
					"one less than number of parse log configurations (because of the default partition)"
					, definition, build)
				return
			}
		}

		for (AdosBuildLogConfig config : definition.options.buildLogConfigs) {
			ReportLocationMatcher matcher = config.getBuildLogMatcher()
			List matchedLogs = getMatchingLogs(definition, build, logs, matcher)

			if (matchedLogs.isEmpty()) {
				warn("No log(s) matched by $matcher. Remember that the regex must match the complete name, " +
					"not just a part! The name of all available logs are: ${logs.name}", definition, build)
				continue;
			}

			String content = downloadBuildLogs(definition, build, matchedLogs)
			if (content.isEmpty()) {
				warn("Downloaded logs are empty: $matcher", definition, build)
				continue
			}

			IAdosBuildLogFilter buildLogFilter = config.getLogContentFilter()
			if (buildLogFilter != null) {
				log("Filtering logs", definition, build)
				content = buildLogFilter.filter(content)
				if (content.isEmpty()) {
					warn("Filtered log is empty: $matcher with $buildLogFilter", definition, build)
					continue
				}
			}

			def logAnalyzer = LogAnalyzerFactory.getFor(matcher.type, TeamscaleExtension.getFrom(project))

			log("Parsing logs", definition, build)
			Set<TeamscaleFinding> findings = parseLog(content, logAnalyzer)

			boolean noFindings = findings.size() == 0
			boolean dockerLog = config.logType == AdosBuildLogConfig.ELogType.DOCKER
			if (noFindings && dockerLog && content.contains("Using cache")) {
				log("Skipping docker log because a cached version is being used", definition, build)
				continue
			}

			upload(definition, build, findings, matcher)
		}
	}

	/**
	 * Download the content of the build logs, which have been matched by a name pattern
	 */
	static String downloadBuildLogs(AdosDefinition definition, AdosBuild build, List<Object> matchedLogs) {
		String content = ""

		matchedLogs.each { log ->
			def currentLine = 0
			while (currentLine < log.lineCount) {
				def endLine = currentLine + MAX_LOG_LINES
				log("Downloading logs ($endLine/${log.lineCount})", definition, build)
				content += definition.http.downloadLog(build.id, "$log.id", currentLine, endLine)

				// endLine is inclusive. Add one in order to prevent parsing a line twice
				currentLine += MAX_LOG_LINES + 1
			}
		}

		return content
	}

	/**
	 * Returns a list of all logs of the build which matched the "logNamePattern"
	 */
	static List getMatchingLogs(AdosDefinition definition, AdosBuild build, List logs, ReportLocationMatcher matcher) {
		def matchingLogs = logs.findAll {
			def nameMatches = matcher.pathMatches(it.name)
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
		return definition.options.buildLogConfigs.size() > 0
	}

	@Override
	List<AdosDefinition> getDefinitions() {
		return TeamscaleExtension.getFrom(project).azureDevOps.definitions
	}
}