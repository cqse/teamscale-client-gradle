package com.teamscale.gradle.teamscale.data

import groovy.transform.Canonical
import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode
@Canonical
class TeamscaleFinding {
	String findingTypeId
	String message
	String path
	String startLine
	String endLine
	String endColumn
	String startOffSet
	String endOffSet

	void removePathSuffix(String suffix) {
		path = path.replace(suffix, "")
	}
}
