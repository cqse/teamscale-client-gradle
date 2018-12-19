package com.teamscale.gradle.azureDevOps.utils.loganalyzer

import static ELogAnalyzerType.CSHARP

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


