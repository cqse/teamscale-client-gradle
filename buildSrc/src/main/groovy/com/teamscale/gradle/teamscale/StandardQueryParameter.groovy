package com.teamscale.gradle.teamscale

import groovy.transform.Canonical

@Canonical
class StandardQueryParameter {
	String partition
	String message
	String t

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
