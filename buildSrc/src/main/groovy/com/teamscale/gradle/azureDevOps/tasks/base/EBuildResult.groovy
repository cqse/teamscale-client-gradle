package com.teamscale.gradle.azureDevOps.tasks.base

enum EBuildResult {
	SUCCEEDED("succeeded"),
	PARTIALLY_SUCCEEDED("partiallySucceeded"),
	FAILED("failed"),
	IGNORE(""); // Ignore the build with the given result

	final String value

	EBuildResult(String value) {
		this.value = value
	}

	static EBuildResult fromString(String resultString) {
		for (EBuildResult result : values()) {
			if(result.value.toLowerCase() == resultString.toLowerCase()) {
				return result
			}
		}
		return IGNORE
	}
}
