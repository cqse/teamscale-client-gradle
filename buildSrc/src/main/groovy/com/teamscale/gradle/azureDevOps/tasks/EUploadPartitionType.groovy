package com.teamscale.gradle.azureDevOps.tasks

enum EUploadPartitionType {
	BUILD("Build"),
	BUILD_FINDINGS("Build Findings"),
	TEST_RESULTS("Test Results"),
	TEST_COVERAGE("Test Coverage")

	String content

	EUploadPartitionType(String content) {
		this.content = content
	}
}
