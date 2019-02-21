package com.teamscale.gradle.azureDevOps.tasks.xaml

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class ProcessedFile {

	final Path file

	Set<String> processed = [] as Set

	public static final String PROCESSED_FILE_NAME = "processed"

	ProcessedFile(Path buildDir) {
		file = Paths.get(buildDir.toString(), PROCESSED_FILE_NAME)
		if(Files.exists(file)) {
			load()
		}
	}

	private void load() {
		processed = file.readLines() as Set
	}

	void save() {
		file.write processed.join("\n")
	}

	void add(String processedArchive) {
		processed.add(processedArchive)
	}

	boolean notProcessed(String archive) {
		return !processed.contains(archive)
	}
}
