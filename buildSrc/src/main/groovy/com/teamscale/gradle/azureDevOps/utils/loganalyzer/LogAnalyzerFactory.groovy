package com.teamscale.gradle.azureDevOps.utils.loganalyzer

class LogAnalyzerFactory {
	static ILogAnalyzer getFor(String type) {
		switch (type.toUpperCase()) {
			case "CSHARP":
				return CSharpLogAnalyzer.getInstance()
			default:
				throw new IllegalArgumentException("There is no log analyzer for $type")
		}
	}
}


