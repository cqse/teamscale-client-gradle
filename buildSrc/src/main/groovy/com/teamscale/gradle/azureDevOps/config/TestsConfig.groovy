package com.teamscale.gradle.azureDevOps.config

class TestsConfig {
	TypeAndPattern resultOptions
	TypeAndPattern coverageOptions

	def result(String type, String filePattern = null, String artifactPattern = null) {
		assert resultOptions == null: "test result can only be set once"
		resultOptions = new TypeAndPattern<>(type, filePattern, artifactPattern)
	}

	def coverage(String type, String filePattern = null, String artifactPattern = null) {
		assert coverageOptions == null: "test coverage can only be set once"
		coverageOptions = new TypeAndPattern<>(type, filePattern, artifactPattern)
	}
}

class TypeAndPattern {
	final String type

	// TODO: make pattern (regex and ant style globbing)
	final String filePattern

	String artifactPattern

	TypeAndPattern(String type, String filePattern, String artifactPattern = null) {
		this.type = type
		this.filePattern = filePattern
		this.artifactPattern = artifactPattern
	}

	boolean matches(String fileName) {
		return fileName ==~ filePattern
	}

	boolean artifactMatches(String artifactName) {
		return artifactName ==~ artifactPattern
	}

	boolean mustSearchInArtifact() {
		return artifactPattern != null
	}
}