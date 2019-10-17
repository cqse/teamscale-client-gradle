package com.teamscale.gradle.azureDevOps.utils;

/**
 * Matches which ADOS build step logs should be analyzed and filters the content of the logs.
 */
class AdosBuildLogConfig {
	/**
	 * Matches which build log from which build step should be taken
	 */
	ReportLocationMatcher buildLogMatcher;

	/**
	 * Filters which content of the matched build logs should be taken for the parsing of the findings.
	 */
	IAdosBuildLogFilter logContentFilter;

	/**
	 * Type of the logs
	 */
	ELogType logType;

	AdosBuildLogConfig(ELogType logType, ReportLocationMatcher buildLogMatcher,
					   IAdosBuildLogFilter logContentFilter = null) {
		this.logType = logType
		this.buildLogMatcher = buildLogMatcher
		this.logContentFilter = logContentFilter
	}

	static enum ELogType {
		STANDARD,
		DOCKER
	}
}
