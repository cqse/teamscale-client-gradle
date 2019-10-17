package com.teamscale.gradle.azureDevOps.utils

import java.util.regex.Pattern

class DockerLogContentFilter implements IAdosBuildLogFilter {
	/**
	 * Pattern matching a docker command of a step
	 */
	private final Pattern stepCommandPattern

	/**
	 * Pattern used for splitting the build log into the different steps
	 */
	private final String stepPattern = "Step\\s[0-9]+/[0-9]+\\s:\\s"

	DockerLogContentFilter(String stepCommandPattern) {
		this.stepCommandPattern = Pattern.compile(stepCommandPattern, Pattern.DOTALL)
	}

	@Override
	String filter(String content) {
		List<String> stepByStep = content.split(stepPattern)
		List<String> matches = stepByStep.findAll { step ->
			List<String> lines = step.split("\n")
			// First line should contain the command
			if (lines.size() > 0 && stepCommandPattern.matcher(lines.get(0)).matches()) {
				return true
			}
			return false
		}
		return matches.join("\n")
	}

	@Override
	String toString() {
		return stepCommandPattern
	}
}
