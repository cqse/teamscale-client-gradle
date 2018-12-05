package com.teamscale.gradle.azureDevOps.data

import groovy.json.internal.LazyMap

import java.time.Instant

class Build {
	String id
	String buildNumber
	String result
	Instant startTime
	Instant finishTime
	Instant queueTime
	String sourceBranch
	String targetBranch
	String uri

	Build(LazyMap data) {
		metaClass.properties.findAll { it.name != "class" && data.containsKey(it.name) }.each {
			Object value
			if(it.type.equals(String)) {
				value = data[it.name]
			} else if(it.type.equals(Instant)) {
				value = Instant.parse(data[it.name])
			}

			it.setProperty(this, value)
		}
	}

	String toString() {
		def props = new HashMap<>(this.properties)
		props.remove("class")
		return props
	}

	/** Returns the time the build took in seconds */
	long getExecutionTime() {
		return (finishTime.toEpochMilli() - startTime.toEpochMilli()) / 1000
	}
}