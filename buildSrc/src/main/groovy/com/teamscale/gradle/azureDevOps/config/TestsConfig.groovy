package com.teamscale.gradle.azureDevOps.config

class TestsConfig {
	ReportLocationMatcher resultOptions
	ReportLocationMatcher coverageOptions

	def result(String type, String pathPattern = null, String artifactPattern = null) {
		assert resultOptions == null: "test result can only be set once"
		resultOptions = new ReportLocationMatcher(type, pathPattern, artifactPattern)
	}

	def coverage(String type, String pathPattern = null, String artifactPattern = null) {
		assert coverageOptions == null: "test coverage can only be set once"
		coverageOptions = new ReportLocationMatcher(type, pathPattern, artifactPattern)
	}
}