package com.teamscale.gradle.azureDevOps.extensions

import com.teamscale.gradle.azureDevOps.utils.ReportLocationMatcher

class TestsConfig {
	/** Configurations for the fetching and uploading of test results */
	List<ReportLocationMatcher> resultConfigs = new ArrayList<>()

	/** Configuration for the fetching and uploading of code coverage from tests */
	List<ReportLocationMatcher> coverageConfigs = new ArrayList<>()

	/** Configuration for the fetching and uploading of test results from release builds */
	ReportLocationMatcher releaseResultConfigs

	def result(String type, String pathPattern = null, String artifactPattern = null, String partition = null) {
		if (pathPattern == null) {
			pathPattern = getDefaultResultPattern(type);
		}

		resultConfigs.add(new ReportLocationMatcher(type, pathPattern, artifactPattern, partition))
	}

	def coverage(String type, String pathPattern = null, String artifactPattern = null, String partition = null) {
		if (pathPattern == null) {
			pathPattern = getDefaultCoveragePattern(type);
		}

		coverageConfigs.add(new ReportLocationMatcher(type, pathPattern, artifactPattern, partition))
	}

	def release(String type, String pathPattern = null, String artifactPattern = null, String partition = null) {
		releaseResultConfigs = new ReportLocationMatcher(type, pathPattern, artifactPattern, partition)
	}

	def getDefaultResultPattern(String type) {
		switch (type) {
			case "JUNIT":
				return ".*\\.xml"
			case "MS_TEST":
				return ".*\\.trx"
			default:
				return null
		}
	}

	def getDefaultCoveragePattern(String type) {
		switch (type) {
			case "VS_COVERAGE":
			case "MS_COVERAGE":
				return ".*\\.coverage"
			case "JACOCO":
				return ".*\\.xml"
		}
	}
}