package com.teamscale.gradle.azureDevOps.config

class BuildOptions {
	/** Name of the definition */
	String name

	Closure branchMapping = { it }

	String logNamePattern

	TestsConfig tests

	// TODO: make this mandatory?
	String partition

	BuildOptions(String name) {
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
}