package com.teamscale.gradle.azureDevOps.config

class TestsConfig {
	TypeAndPattern result
	TypeAndPattern coverage

	def result(String type, String extension = null) {
		result = getTypeAndExtension(type, extension)
	}

	def coverage(String type, String extension = null) {
		assert coverage == null: "coverage can only be set once"
		coverage = getTypeAndExtension(type, extension)
	}

	static getTypeAndExtension(String type, String pattern) {
		try {
			def enumType = EDefaultPattern.valueOf(type)
			if (!pattern) {
				pattern = enumType.getPattern()
			}

			return new TypeAndPattern<>(type: enumType, pattern: pattern)
		} catch (IllegalArgumentException e) {
			// TODO: create exception for this
			throw e
		}
	}

	def isTestResultFile(String fileName) {
		if(result) {
			return fileName ==~ result.pattern
		}
		return false
	}
}

enum EDefaultPattern {
	JUNIT(/.*\.xml$/), MS_TEST(/.*\.trx$/), VS_COVERAGE(/.*\.coverage$/)

	String pattern

	EDefaultPattern(String pattern) {
		this.pattern = pattern
	}
}

class TypeAndPattern {
	String type
	// TODO: make pattern (Regex AND [MP!!] ant)
	String pattern
}
