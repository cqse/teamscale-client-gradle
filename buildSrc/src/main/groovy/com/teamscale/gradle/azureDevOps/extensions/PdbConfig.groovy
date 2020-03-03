package com.teamscale.gradle.azureDevOps.extensions

import com.teamscale.gradle.azureDevOps.utils.ReportLocationMatcher

class PdbConfig {
	ReportLocationMatcher versionConfig
	ReportLocationMatcher pdbConfig

	def version(String artifactPattern, String pathPattern) {
		versionConfig = new ReportLocationMatcher(null, pathPattern, artifactPattern)
	}

	def pdb(String artifactPattern, String pathPattern) {
		pdbConfig = new ReportLocationMatcher(null, pathPattern, artifactPattern)
	}
}