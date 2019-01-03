package com.teamscale.gradle.azureDevOps.utils

class AzureBuildException extends Exception {
	AzureBuildException(String message) {
		super(message)
	}

	AzureBuildException(String message, Throwable t) {
		super(message, t)
	}
}
