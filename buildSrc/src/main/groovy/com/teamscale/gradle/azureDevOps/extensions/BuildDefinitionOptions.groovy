package com.teamscale.gradle.azureDevOps.extensions

import com.teamscale.gradle.azureDevOps.utils.ReportLocationMatcher

class BuildDefinitionOptions {
	/** Name of the definition */
	String name

	Closure branchMapping = { it }

	/**
	 * The pattern for the name of the build step whose logs should be analyzed for
	 * build findings
	 */
	ReportLocationMatcher logNameMatcher

	TestsConfig tests

	String partition

	List<ReportLocationMatcher> reports = new ArrayList<>()

	/** Maximum number of days  */
	int maxDaysBetweenBuilds = 30

	BuildDefinitionOptions(String name) {
		this.name = name
	}

	@Override
	String toString() {
		def props = new HashMap<>(this.properties)
		props.remove("class")
		return props
	}

	def tests(Closure closure) {
		tests = new TestsConfig()
		closure.delegate = tests
		closure.resolveStrategy = Closure.DELEGATE_ONLY
		closure.run()
	}

	/** Define the log analyzer for this definition */
	def parseLogs(String type, String logNamePattern) {
		this.logNameMatcher = new ReportLocationMatcher(type, logNamePattern)
	}

	/** Define the location and type of a report which should be uploaded */
	def report(String type, String pathPattern, String artifactPattern, String partition = null) {
		reports.add(new ReportLocationMatcher(type, pathPattern, artifactPattern, partition))
	}

	def partition(String partition) {
		this.partition = partition
	}
}