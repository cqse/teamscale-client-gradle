package com.teamscale.gradle.azureDevOps.config

import com.teamscale.gradle.azureDevOps.utils.AzureBuildException
import com.teamscale.gradle.azureDevOps.utils.loganalyzer.ELogAnalyzerType
import com.teamscale.gradle.azureDevOps.utils.loganalyzer.ILogAnalyzer
import com.teamscale.gradle.azureDevOps.utils.loganalyzer.LogAnalyzerFactory

import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

class DefinitionOptions {
	/** Name of the definition */
	String name

	Closure branchMapping = { it }

	Pattern logNamePattern

	TestsConfig tests

	String partition

	List<ReportConfig> reports = new ArrayList<>()

	/** Maximum number of days  */
	int maxDaysBetweenBuilds = 30

	/**
	 * Instance of log analyzer for this project.
	 * The log analyzer parses build task logs for any possible finding which can be uploaded to teamscale.
	 */
	ILogAnalyzer logAnalyzer

	DefinitionOptions(String name) {
		this.name = name
	}

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
		try {
			this.logNamePattern = ~logNamePattern
		} catch (PatternSyntaxException e) {
			new AzureBuildException("'$logNamePattern' is no a valid regex", e)
		}

		def logAnalyzerType = ELogAnalyzerType.valueOf(type)
		logAnalyzer = LogAnalyzerFactory.getLogAnalyzer(logAnalyzerType)
	}

	/** Define the location and type of a report which should be uploaded */
	def report(String type, String pathPattern, String artifactPattern, String partition = null) {
		reports.add(new ReportConfig(type, pathPattern, artifactPattern, partition))
	}
}