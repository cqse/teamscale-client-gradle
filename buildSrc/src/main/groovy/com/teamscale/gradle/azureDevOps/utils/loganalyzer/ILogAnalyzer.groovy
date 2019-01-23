package com.teamscale.gradle.azureDevOps.utils.loganalyzer

import com.teamscale.gradle.teamscale.data.TeamscaleFinding

interface ILogAnalyzer {
	/**
	 * Parses the given string and tries to extract a finding. If it cannot match, <code>null</code>
	 * is returned.
	 */
	TeamscaleFinding analyze(String logLine)
}