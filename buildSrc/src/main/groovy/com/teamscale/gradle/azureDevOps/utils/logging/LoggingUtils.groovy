package com.teamscale.gradle.azureDevOps.utils.logging

import com.teamscale.gradle.azureDevOps.data.Build
import com.teamscale.gradle.azureDevOps.data.Definition
import groovy.json.JsonBuilder

class LoggingUtils {
	static String createMessage(String message, Definition definition = null, Build build = null) {
		def info = ""

		if (definition) {
			info += "[$definition.name]"
		}

		if (build) {
			info += "[$build.buildNumber]"
		}

		if (info.length() > 0) {
			info += " "
		}

		return "$info$message"
	}

	static void warn(String message, Definition definition = null, Build build = null) {
		String prefix = "[WARN]"
		if (!definition && !build) {
			prefix += " "
		}

		println prefix + createMessage(message, definition, build)
	}

	static void log(String message, Definition definition = null, Build build = null) {
		println createMessage(message, definition, build)
	}

	static void debug(String message) {
		String prefix = "[DEBUG] "
		log(prefix + message)
	}

	static pprint(input) {
		println new JsonBuilder(input).toPrettyString()
	}
}
