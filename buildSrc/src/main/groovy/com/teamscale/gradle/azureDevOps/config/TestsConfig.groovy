package com.teamscale.gradle.azureDevOps.config

class TestsConfig {
	TypeAndPattern resultOptions
	TypeAndPattern coverageOptions

	def result(String type, String pathPattern = null, String artifactPattern = null) {
		assert resultOptions == null: "test result can only be set once"
		resultOptions = new TypeAndPattern<>(type, pathPattern, artifactPattern)
	}

	def coverage(String type, String pathPattern = null, String artifactPattern = null) {
		assert coverageOptions == null: "test coverage can only be set once"
		coverageOptions = new TypeAndPattern<>(type, pathPattern, artifactPattern)
	}
}

class TypeAndPattern {
	final String type

	// TODO: make pattern (regex and ant style globbing)
	final String pathPattern

	String artifactPattern

	TypeAndPattern(String type, String pathPattern, String artifactPattern = null) {
		this.type = type
		this.pathPattern = pathPattern
		this.artifactPattern = artifactPattern
	}

	boolean pathMatches(String path) {
		return path ==~ pathPattern
	}

	boolean artifactMatches(String artifactName) {
		return artifactName ==~ artifactPattern
	}

	boolean mustSearchInArtifact() {
		return artifactPattern != null
	}
}