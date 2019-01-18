package com.teamscale.gradle.azureDevOps.config

class ReportConfig extends ReportLocationMatcher {
	final String partition

	ReportConfig(String type, String pathPattern, String artifactPattern, String partition = null) {
		super(type, pathPattern, artifactPattern)

		if(partition) {
			this.partition = partition
		} else {
			this.partition = "(${this.type})"
		}
	}

	@Override
	String toString() {
		String string = "[type: $type, path-pattern: $pathPattern, partition: $partition"
		if(artifactPattern) {
			string += ", artifact-pattern: $artifactPattern"
		}
		return string + "]"
	}
}