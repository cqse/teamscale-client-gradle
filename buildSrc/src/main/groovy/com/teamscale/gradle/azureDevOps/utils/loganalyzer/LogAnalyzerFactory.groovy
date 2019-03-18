package com.teamscale.gradle.azureDevOps.utils.loganalyzer

import com.teamscale.gradle.teamscale.data.TeamscaleExtension

class LogAnalyzerFactory {
	static ILogAnalyzer getFor(String type, TeamscaleExtension extension) {
		switch (type.toUpperCase()) {
			case "CSHARP":
				return new CSharpLogAnalyzer(extension)
			default:
				throw new IllegalArgumentException("There is no log analyzer for $type")
		}
	}
}


