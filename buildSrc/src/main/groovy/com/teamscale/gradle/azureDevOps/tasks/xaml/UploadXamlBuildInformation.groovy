package com.teamscale.gradle.azureDevOps.tasks.xaml

import com.teamscale.gradle.azureDevOps.extensions.XamlDefinitionOptions
import com.teamscale.gradle.azureDevOps.extensions.XamlProjectConfig
import com.teamscale.gradle.azureDevOps.utils.ZipUtils
import com.teamscale.gradle.azureDevOps.utils.loganalyzer.CSharpLogAnalyzer
import com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils
import com.teamscale.gradle.teamscale.TeamscaleClient
import com.teamscale.gradle.teamscale.data.TeamscaleExtension

import java.nio.file.Files
import java.nio.file.Path

class UploadXamlBuildInformation extends XamlTask {
	final static String TASK_NAME = "uploadXamlBuildInformation"

	@Override
	void process(Path buildDir, XamlProjectConfig adosProject, XamlDefinitionOptions definition) {
		if (!Files.exists(buildDir)) {
			// TODO: Log warning
			Files.createDirectories(buildDir)
		}
		ProcessedFile processed = new ProcessedFile(buildDir)

		(Files.list(buildDir)
			.findAll { Path path ->
			path.fileName.toString().endsWith("zip") && processed.notProcessed(path.fileName.toString())
		} as List<Path>).each { Path archive ->
			LoggingUtils.log("Processing: $archive.fileName", project.name, definition.name)

			if (processZip(archive, TeamscaleExtension.getFrom(this.project).http, definition)) {
				// TODO Process
				//processed.add(file.fileName.toString())
			}
		}

		processed.save()
	}

	static boolean processZip(Path archive, TeamscaleClient http, XamlDefinitionOptions definition) {
		uploadBuildStatus(archive, http, definition)
	}

	static void uploadBuildStatus(Path archive, TeamscaleClient http, XamlDefinitionOptions definition) {
		Path errors = ZipUtils.getMatchesInArchive(archive.toAbsolutePath().toString(), definition.errors)
		if (errors.text.size() == 0) {
			// build succeeded
		}
		// build failed
	}

	static void uploadReports(Path archive, TeamscaleClient http, XamlDefinitionOptions definition) {
		definition.reports.each { matcher ->
			List<Path> reports = ZipUtils.getMatchesInArchive(archive.toAbsolutePath().toString(), matcher)
		}
	}

	static void uploadBuildFindings(Path archive, TeamscaleClient http, XamlDefinitionOptions definition) {
		Path warnings = ZipUtils.getMatchesInArchive(archive.toAbsolutePath().toString(), definition.warnings)
		CSharpLogAnalyzer analyzer = new CSharpLogAnalyzer()
	}
}
