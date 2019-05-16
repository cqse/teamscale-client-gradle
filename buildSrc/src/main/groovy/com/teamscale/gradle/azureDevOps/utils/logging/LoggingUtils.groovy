package com.teamscale.gradle.azureDevOps.utils.logging


import com.teamscale.gradle.azureDevOps.data.IBuild
import com.teamscale.gradle.azureDevOps.data.IDefinition
import groovy.json.JsonBuilder

/** Some functions to make the logging across the plugin uniform */
class LoggingUtils {

	/**
	 * Creates a logging message with a give message and a build, as well as its definitionName
	 */
	static String createMessage(String message, IDefinition definition = null, IBuild build = null) {
		def info = ""

		if (definition) {
			info += "[${definition.getName()}]"
		}

		if (build) {
			info += "[${build.getName()}]"
		}

		if (info.length() > 0) {
			info += " "
		}

		return "$info$message"
	}

	/** Logs a warning */
	static void warn(String message, IDefinition definition = null, IBuild build = null) {
		println "[WARN] " + createMessage(message, definition, build)
	}

	/** Simple output */
	static void log(String message, IDefinition definition = null, IBuild build = null) {
		println createMessage(message, definition, build)
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
