package com.teamscale.gradle.azureDevOps.config

class TestsConfig {
	ReportLocationConfig resultOptions
	ReportLocationConfig coverageOptions
	ReportLocationConfig releaseResultOptions

	def result(String type, String pathPattern = null, String artifactPattern = null, String partition = null) {
		assert resultOptions == null: "test result can only be configured once"
		resultOptions = new ReportLocationConfig(type, pathPattern, artifactPattern, partition)
	}

	def coverage(String type, String pathPattern = null, String artifactPattern = null, String partition = null) {
		assert coverageOptions == null: "test coverage can only be configured once"
		coverageOptions = new ReportLocationConfig(type, pathPattern, artifactPattern, partition)
	}

	def release(String type, String pathPattern = null, String artifactPattern = null, String partition = null) {
		assert releaseResultOptions == null: "release test results can only be configured once"
		releaseResultOptions = new ReportLocationConfig(type, pathPattern, artifactPattern, partition)
	}
}