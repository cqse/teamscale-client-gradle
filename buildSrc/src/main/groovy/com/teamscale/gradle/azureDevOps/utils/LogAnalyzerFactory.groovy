package com.teamscale.gradle.azureDevOps.utils

import com.teamscale.gradle.teamscale.TeamscaleFinding

import static com.teamscale.gradle.azureDevOps.utils.ELogAnalyzerType.CSHARP

class LogAnalyzerFactory {
	static ILogAnalyzer getLogAnalyzer(ELogAnalyzerType type) {
		switch (type) {
			case CSHARP:
				return new CSharpLogAnalyzer()
			default:
				throw new IllegalArgumentException("There is no log analyzer for $type")
		}
	}
}

interface ILogAnalyzer {
	/**
	 * Parses the given string and tries to extract a finding. If it cannot match, <code>null</code> is
	 * returned.
	 */
	TeamscaleFinding analyze(String logLine)
}

enum ELogAnalyzerType {
	CSHARP
}
