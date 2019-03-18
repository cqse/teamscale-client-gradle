package com.teamscale.gradle.azureDevOps.extensions

class AdosConfig {
	List<String> csharpFindings = []

	def csharpFindings(List<String> findingIds) {
		csharpFindings = findingIds
	}
}
