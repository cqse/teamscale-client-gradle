package com.teamscale.gradle.azureDevOps.utils

import com.teamscale.gradle.teamscale.TeamscaleFinding

import java.nio.file.Paths
import java.util.regex.Matcher
import java.util.regex.Pattern

class CSharpLogAnalyzer implements ILogAnalyzer {
	final static String DATE_TIME = "[0-9-\\.:T]{18,}[A-Z]{1}"

	final static String LOCATION = "((?:\\w:)?(?:[^:#\\s]+)\\\\?\\w+\\.\\w+)\\(([0-9]+),[0-9,]+\\):"

	final static String FINDING_ID = "(?:[\\w\\s]+)?warning\\s((?:[A-Z]{2,3})[0-9]+):"

	final static String MESSAGE = "(.*?)\\s+\\[[^\\]]*\\]"

	final static Pattern COMBINED = ~"$DATE_TIME\\s+$LOCATION\\s$FINDING_ID\\s+$MESSAGE"

	@Override
	TeamscaleFinding analyze(String logLine) {
		Matcher matcher = COMBINED.matcher(logLine)
		if (matcher.find()) {
			// TODO: remove findings with the wrong file ending?

			def filePath = matcher.group(1).replaceAll("\\\\", "/")
			def line = matcher.group(2)
			def findingId = matcher.group(3)
			def message = matcher.group(4)

			return new TeamscaleFinding(findingTypeId: findingId, message: message, path: filePath, startLine: line, endLine: line)
		}

		return null
	}
}
