package com.teamscale.gradle.azureDevOps.extensions

import com.teamscale.gradle.azureDevOps.utils.ReportLocationMatcher

class XamlDefinitionOptions {
	String name

	String partition

	String branch

	ReportLocationMatcher warnings

	ReportLocationMatcher errors

	ReportLocationMatcher result

	ReportLocationMatcher coverage

	XamlDefinitionOptions(String name) {
		this.name = name
	}

	def partition(String partition) {
		this.partition = partition
	}

	def uploadToBranch(String branch) {
		this.branch = branch
	}

	def warnings(String type, String pattern) {
		warnings = new ReportLocationMatcher(type, pattern)
	}

	def errors(String pattern) {
		errors = new ReportLocationMatcher(null, pattern)
	}

	def result(String type, String pattern, String partition = null) {
		result = new ReportLocationMatcher(type, pattern, null, partition)
	}

	def coverage(String type, String pattern, String partition = null) {
		coverage = new ReportLocationMatcher(type, pattern, partition)
	}
}
