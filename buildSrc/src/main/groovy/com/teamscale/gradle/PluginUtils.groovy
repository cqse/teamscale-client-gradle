package com.teamscale.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.Task

class PluginUtils {
	/**
	 * Create a task for the project.
	 * Optional parameters can set a group and dependencies to other tasks
	 */
	static Task createTask(Project project, Class<DefaultTask> cls, String group = null, Object... dependsOn) {
		Task task = project.tasks.create((String) cls.TASK_NAME, cls)
		if (dependsOn) {
			task.dependsOn(dependsOn)
		}

		if (group) {
			task.group = group
		}

		return task
	}
}
