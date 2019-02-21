package com.teamscale.gradle.azureDevOps.utils

import java.util.regex.Pattern

class ReportLocationMatcher {
	/**
	 * The partition the report will be uploaded to.
	 */
	final String partition

	/**
	 * The report type which must match any of the EReportTypeFormat in teamscale.
	 * If not, the upload of the report will fail.
	 */
	final String type

	/**
	 * Pattern which must match the file name or the complete path of the file.
	 * A complete path match must be given when the report is inside of an artifact!
	 */
	final Pattern pathPattern

	/**
	 * The pattern for matching any published artifact of a build.
	 * This is optional, because the report can probably downloaded directly from ADOS and is only
	 * necessary if the report is only available in an artifact.
	 */
	Pattern artifactPattern = null

	ReportLocationMatcher(String type, String pathPattern, String artifactPattern = null, String partition = null) {
		this.type = type
		this.partition = partition

		this.pathPattern = BuildUtils.getPatternFromRegexOrAntGlobbing(pathPattern)
		checkPattern(this.pathPattern)

		if (artifactPattern) {
			this.artifactPattern = BuildUtils.getPatternFromRegexOrAntGlobbing(artifactPattern)
			checkPattern(this.artifactPattern)
		}
	}

	@Override
	String toString() {
		String string = "[type: $type, path-pattern: $pathPattern, partition: $partition"
		if (artifactPattern) {
			string += ", artifact-pattern: $artifactPattern"
		}

		if (partition) {
			string += ", partition: $partition"
		}
		return string + "]"
	}

	private static void checkPattern(Pattern pattern) {
		assert pattern != null: "The given pattern $pattern is not a valid regex or ant-style " +
			"globbing pattern"
	}

	boolean pathMatches(String path) {
		return path ==~ pathPattern
	}

	boolean artifactMatches(String artifactName) {
		return artifactName ==~ artifactPattern
	}

	/**
	 * If an artifact pattern is given the file is located inside of an artifact and cannot be located
	 * not via any other REST ADOS service call.
	 */
	boolean mustSearchInArtifact() {
		return artifactPattern != null
	}
}