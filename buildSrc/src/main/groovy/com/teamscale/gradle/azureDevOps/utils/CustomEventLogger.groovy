package com.teamscale.gradle.azureDevOps.utils

import org.gradle.BuildAdapter
import org.gradle.BuildResult
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.tasks.TaskState

class CustomEventLogger extends BuildAdapter implements TaskExecutionListener {

	void beforeExecute(Task task) {
		println ""
		println task
	}

	void afterExecute(Task task, TaskState state) {}

	void buildFinished(BuildResult result) {
		println ""
		println 'build completed'
		if (result.failure != null) {
			result.failure.printStackTrace()
		}
	}
}