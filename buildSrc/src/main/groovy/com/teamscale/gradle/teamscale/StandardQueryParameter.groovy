package com.teamscale.gradle.teamscale

import groovy.transform.Canonical

/**
 * Obligatory parameters for most service calls to teamscale
 */
@Canonical
class StandardQueryParameter {
	String partition
	String message
	String t

	/** Return the Object as a Map of its fields and values */
	Map<String, String> asMap() {
		return this.getClass()
			.declaredFields
			.findAll { !it.synthetic }
			.collectEntries { field ->
			[field.name, this."$field.name"]
		}
	}

	void appendToMessage(String append) {
		this.message += ": $append"
	}
}
