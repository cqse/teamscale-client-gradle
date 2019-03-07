package com.teamscale.gradle.azureDevOps.utils.loganalyzer

import com.teamscale.gradle.teamscale.data.TeamscaleFinding

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Can analyze C# logs and extract possible findings which can be then uploaded to
 * teamscale.
 */
class CSharpLogAnalyzer implements ILogAnalyzer {
	final static String DATE_TIME = "[0-9-\\.:T]{18,}[A-Z]{1}"
	final static String PATH = "((?:\\w:)?(?:[^:#\\s]+)\\\\?\\w+\\.\\w+)\\(([0-9]+),[0-9,]+\\):"
	final static String FINDING_ID = "(?:[\\w\\s]+)?warning\\s((?:[A-Z]{2,3})[0-9]+):"
	final static String MESSAGE = "(.*?)\\s+\\[[^\\]]*\\]"
	final static Pattern COMBINED = ~"(?:$DATE_TIME\\s+)?$PATH\\s$FINDING_ID\\s+$MESSAGE"

	private final static CSharpLogAnalyzer instance = new CSharpLogAnalyzer()

	@Override
	TeamscaleFinding analyze(String logLine) {
		Matcher matcher = COMBINED.matcher(logLine)
		if (matcher.find()) {
			def filePath = matcher.group(1).replaceAll("\\\\", "/")
			def line = matcher.group(2)
			def findingId = matcher.group(3)
			def message = matcher.group(4)

			if(isIncluded(findingId)) {
				return new TeamscaleFinding(findingTypeId: findingId, message: message, path: filePath,
					startLine: line, endLine: line)
			}
		}

		return null
	}

	/** Checks if the finding should be included based on its findings id */
	static boolean isIncluded(String findingId) {
		// Filter out FxCops findings
		if(findingId =~ /^(CA)/) {
			return false
		}
		return true
	}

	static ILogAnalyzer getInstance() {
		return instance
	}
}
