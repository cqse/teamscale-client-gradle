package com.teamscale.gradle.azureDevOps.tasks

enum EUploadPartitionType {
	TEST("Test"), BUILD("Build"), METRICS("Metrics")

	String content

	EUploadPartitionType(String content) {
		this.content = content
	}
}
