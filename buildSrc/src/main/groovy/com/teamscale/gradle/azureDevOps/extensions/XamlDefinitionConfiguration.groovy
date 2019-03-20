package com.teamscale.gradle.azureDevOps.extensions

import com.teamscale.gradle.azureDevOps.utils.ReportLocationMatcher

/**
 * Configuration which determines which information of a XAML build should be taken and uploded to
 * teamscale.
 */
class XamlDefinitionConfiguration {
	/** Name of the definition */
	String name

	/**
	 * A suffix to the name of the partition the information of the build will be uploaded to.
	 * This needs to be set if there are two or more definitions for the same project, otherwise
	 * the information of all the builds will be uploaded to the same partition, thus overwriting it.
	 */
	String partition

	/** Teamscale branch the build info will be uploaded to */
	String teamscaleBranch

	/** Matcher for the file containing the warnings logs of the build */
	ReportLocationMatcher warnings

	/** Matcher for the file containing any errors which happened during the build */
	ReportLocationMatcher errors

	/** Matcher for any test result files */
	ReportLocationMatcher result

	/** Matcher for any coverage files */
	ReportLocationMatcher coverage

	/** Matcher for the Build Log where the timestamp can be parsed out of */
	ReportLocationMatcher timestamp

	XamlDefinitionConfiguration(String name) {
		this.name = name
	}

	def partition(String partition) {
		this.partition = partition
	}

	def uploadToBranch(String branch) {
		this.teamscaleBranch = branch
	}

	def warnings(String type, String pattern, String partition = null) {
		warnings = new ReportLocationMatcher(type, pattern, null, partition)
	}

	def errors(String pattern, String partition = null) {
		errors = new ReportLocationMatcher(null, pattern, null, partition)
	}

	def result(String type, String pattern, String partition = null) {
		result = new ReportLocationMatcher(type, pattern, null, partition)
	}

	def coverage(String type, String pattern, String partition = null) {
		coverage = new ReportLocationMatcher(type, pattern, null, partition)
	}

	def timestamp(String pattern) {
		timestamp = new ReportLocationMatcher(null, pattern)
	}
}
