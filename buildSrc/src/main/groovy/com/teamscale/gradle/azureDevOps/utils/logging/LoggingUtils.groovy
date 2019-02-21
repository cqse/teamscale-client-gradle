package com.teamscale.gradle.azureDevOps.utils.logging

import com.teamscale.gradle.azureDevOps.data.Build
import com.teamscale.gradle.azureDevOps.data.Definition
import groovy.json.JsonBuilder

/** Some functions to make the logging across the plugin uniform */
class LoggingUtils {

	/**
	 * Creates a logging message with a give message and a build, as well as its definitionName
	 */
	static String createLogMessage(String message, String definition = null, String build = null) {
		def info = ""

		if (definition) {
			info += "[$definition]"
		}

		if (build) {
			info += "[$build]"
		}

		if (info.length() > 0) {
			info += " "
		}

		return "$info$message"
	}

	static String createMessage(String message, Definition definition, Build build = null) {
		createLogMessage(message, definition?.name, build?.buildNumber)
	}

	/** Logs a warning */
	static void warn(String message) {
		String prefix = "[WARN] "

		println prefix + createLogMessage(message)
	}

	/** Simple output */
	static void log(String message, Definition definition = null, Build build = null) {
		println createMessage(message, definition, build)
	}

	static void log(String message, String definition, String build = null) {
		println createLogMessage(message, definition, build)
	}

	/** Logs a debug message */
	static void debug(String message) {
		String prefix = "[DEBUG] "
		log(prefix + message)
	}

	/** Tries to pretty print the given Object. Especially interesting for Lists or JSON content */
	static pprint(Object input) {
		println new JsonBuilder(input).toPrettyString()
	}
}
