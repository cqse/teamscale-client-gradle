package com.teamscale.gradle.azureDevOps.config

class TestsConfig {
	ReportLocationMatcher resultOptions
	ReportLocationMatcher coverageOptions
	ReportLocationMatcher releaseResultOptions

	def result(String type, String pathPattern = null, String artifactPattern = null) {
		assert resultOptions == null: "test result can only be configured once"
		resultOptions = new ReportLocationMatcher(type, pathPattern, artifactPattern)
	}

	def coverage(String type, String pathPattern = null, String artifactPattern = null) {
		assert coverageOptions == null: "test coverage can only be configured once"
		coverageOptions = new ReportLocationMatcher(type, pathPattern, artifactPattern)
	}

	def release(String type, String pathPattern = null) {
		assert releaseResultOptions == null: "release test results can only be configured once"
		releaseResultOptions = new ReportLocationMatcher(type, pathPattern)
	}
}