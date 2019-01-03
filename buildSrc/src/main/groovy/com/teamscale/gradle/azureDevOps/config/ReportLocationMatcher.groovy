package com.teamscale.gradle.azureDevOps.config

import com.teamscale.gradle.azureDevOps.utils.BuildUtils

import java.util.regex.Pattern

/**
 * Provides the config and methods for matching the location of a report which is either simply defined
 * by a file name or located inside of an artifact.
 */
class ReportLocationMatcher {
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

	@Override
	String toString() {
		return "type: $type, path-pattern: $pathPattern, artifact-pattern: $artifactPattern"
	}

	ReportLocationMatcher(String type, String pathPattern, String artifactPattern = null) {
		this.type = type
		this.pathPattern = BuildUtils.getPatternFromRegexOrAntGlobbing(pathPattern)
		checkPattern(this.pathPattern)

		if(artifactPattern) {
			this.artifactPattern = BuildUtils.getPatternFromRegexOrAntGlobbing(artifactPattern)
			checkPattern(this.artifactPattern)
		}
	}

	private static void checkPattern(Pattern pattern) {
		assert pattern != null: "The given pattern $pattern is not a valid regex or 	ant-style " +
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