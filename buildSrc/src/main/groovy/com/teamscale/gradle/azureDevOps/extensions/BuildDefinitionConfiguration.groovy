package com.teamscale.gradle.azureDevOps.extensions

import com.teamscale.gradle.azureDevOps.utils.ReportLocationMatcher

class BuildDefinitionConfiguration {
	/** Name of the definition */
	String name

	/**
	 * Closure determining the teamscale branch the information should be uploaded to.
	 * The closure must have one parameter, which is the name of the branch the build was made from in ADOS.
	 */
	Closure branchMapping = { it }

	/**
	 * The pattern for the name of the build step whose logs should be analyzed for
	 * build findings
	 */
	ReportLocationMatcher logNameMatcher

	/** Configuration for test results and coverage */
	TestsConfig tests

	/**
	 * A suffix to the name of the partition the information of the build will be uploaded to.
	 * This needs to be set if there are two or more definitions for the same project, otherwise
	 * the information of all the builds will be uploaded to the same partition, thus overwriting it.
	 */
	String partition

	/** List of matcher for any reports which should be uploaded */
	List<ReportLocationMatcher> reports = new ArrayList<>()

	/** Maximum number of days  */
	int maxDaysBetweenBuilds = 30

	BuildDefinitionConfiguration(String name) {
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
	def parseLogs(String type, String logNamePattern, String partition = null) {
		this.logNameMatcher = new ReportLocationMatcher(type, logNamePattern, null, partition)
	}

	/** Define the location and type of a report which should be uploaded */
	def report(String type, String pathPattern, String artifactPattern, String partition = null) {
		reports.add(new ReportLocationMatcher(type, pathPattern, artifactPattern, partition))
	}

	def partition(String partition) {
		this.partition = partition
	}
}