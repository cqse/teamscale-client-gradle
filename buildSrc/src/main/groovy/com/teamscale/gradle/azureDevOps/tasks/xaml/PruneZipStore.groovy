package com.teamscale.gradle.azureDevOps.tasks.xaml

import com.teamscale.gradle.azureDevOps.extensions.XamlDefinitionOptions
import com.teamscale.gradle.azureDevOps.extensions.XamlProjectConfig

import java.nio.file.Path

class PruneZipStore extends XamlTask {
	final static String TASK_NAME = "pruneZipStore"

	@Override
	void process(Path buildDir, XamlProjectConfig adosProject, XamlDefinitionOptions definition) {
		// Check if there are too many files in the folder

		// Load processed file

		// Check if all of them have been processed yet

		// delete all old processed files
	}
}
