package com.teamscale.gradle.azureDevOps.utils.logging

import com.teamscale.gradle.azureDevOps.data.Build
import com.teamscale.gradle.azureDevOps.data.Definition
import groovy.json.JsonBuilder

class LoggingUtils {
	static void log(String message, Definition definition = null, Build build = null) {
		println createMessage(message, definition, build)
	}

	static String createMessage(String message, Definition definition = null, Build build = null) {
		def info = ""

		if (definition) {
			info += "[$definition.name]"
		}

		if (build) {
//			def targetBranch = build.targetBranch
//			if(targetBranch == "") {
//				targetBranch = "main"
//			}
//			info += "[$build.buildNumber]['$build.sourceBranch'->'$targetBranch']"
			info += "[$build.buildNumber]"
		}

		if(info.length() > 0) {
			info += " "
		}

		return "$info$message"
	}

	static void warn(String message, Definition definition = null, Build build = null) {
		println "[WARN]" + createMessage(message, definition, build)
	}

	static pprint(input) {
		println new JsonBuilder(input).toPrettyString()
	}
}