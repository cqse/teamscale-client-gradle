package com.teamscale.gradle.azureDevOps.extensions

import com.teamscale.gradle.azureDevOps.utils.ReportLocationMatcher

class XamlDefinitionOptions {
	String name

	String partition

	Closure branchMapping = { "" }

	List<ReportLocationMatcher> reports = []

	ReportLocationMatcher warnings = new ReportLocationMatcher(null, "BuildTypes/Warnings.txt")

	ReportLocationMatcher errors = new ReportLocationMatcher(null, "BuildTypes/Errors.txt")

	XamlDefinitionOptions(String name) {
		this.name = name
	}

	def warnings(String type, String pattern) {
		warnings = new ReportLocationMatcher(type, pattern)
	}

	def errors(String type, String pattern) {
		errors = new ReportLocationMatcher(type, pattern)
	}

	def report(String type, String pattern) {
		reports.add(new ReportLocationMatcher(type, pattern))
	}
}
