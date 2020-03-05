package com.teamscale.gradle.azureDevOps.extensions

import com.teamscale.gradle.azureDevOps.utils.ReportLocationMatcher

class PdbConfig {
	ReportLocationMatcher versionMatcher
	ReportLocationMatcher pdbMatcher

	def version(String artifactPattern, String pathPattern) {
		versionMatcher = new ReportLocationMatcher(null, pathPattern, artifactPattern)
	}

	def pdb(String artifactPattern, String pathPattern) {
		pdbMatcher = new ReportLocationMatcher(null, pathPattern, artifactPattern)
	}
}