package com.teamscale.gradle.azureDevOps.utils.logging

import org.gradle.BuildAdapter
import org.gradle.BuildResult
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.tasks.TaskState

/**
 * Simple custom logger which prints a new line before a task.
 * This makes the logs more readable when they are run in jenkins.
 *
 * --console=rich is not a solution as it prints ANSI escape sequences.
 */
class CustomTaskLogger extends BuildAdapter implements TaskExecutionListener {
	@Override
	void beforeExecute(Task task) {
		println ""
		println task
	}

	@Override
	void afterExecute(Task task, TaskState state) {}

	@Override
	void buildFinished(BuildResult result) {
		println ""
		println "build completed"
		if (result.failure != null) {
			result.failure.printStackTrace()
		}
	}
}