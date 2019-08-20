package com.teamscale.gradle.azureDevOps.tasks.xaml

import com.teamscale.gradle.azureDevOps.data.XamlBuild
import com.teamscale.gradle.azureDevOps.data.XamlDefinition
import com.teamscale.gradle.azureDevOps.extensions.AzureDevOpsExtension
import com.teamscale.gradle.azureDevOps.extensions.XamlExtension
import com.teamscale.gradle.azureDevOps.tasks.base.EBuildResult
import com.teamscale.gradle.azureDevOps.utils.ZipUtils
import com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils
import com.teamscale.gradle.teamscale.data.TeamscaleExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.Paths

class ProcessBuildArchivesTask extends DefaultTask {
	final static String TASK_NAME = "processBuildArchives"

	@TaskAction
	def action() {
		AzureDevOpsExtension ados = TeamscaleExtension.getFrom(project).azureDevOps
		XamlExtension xaml = ados.xaml

		assert xaml.inbox: "No inbox defined for the XAML build zips"
		assert xaml.zipStore: "No zip store defined for the XAML build zips"

		xaml.organizations.values().each { organization ->
			organization.projects.values().each { project ->
				project.definitions.values().each { options ->
					Path definitionInbox = Paths.get(xaml.inbox.toString(), project.name, options.name)
					if (!Files.exists(definitionInbox)) {
						LoggingUtils.warn("Inbox \"$definitionInbox\" does not exists, so no builds can be processed")
						return false
					}

					Path buildDir = createZipstoreDirectory(xaml.zipStore.toString(), project.name, options.name)

					def definition = new XamlDefinition(options, buildDir, ados.cache, organization.name, project.name)
					xaml.definitions.add(definition)

					assert definition.config.errors: "A pattern for finding the files with potential errors must be " +
						"supplied in order to determine the build status"

					process(definition)
					fetchAvailableBuilds(definition)
				}
			}
		}
	}

	void process(XamlDefinition definition) {
		XamlExtension xaml = TeamscaleExtension.getFrom(project).azureDevOps.xaml
		Path definitionInbox = Paths.get(xaml.inbox.toString(), definition.getProject(), definition.getName())

		int moved = 0
		Files.list(definitionInbox).each { Path file ->
			if (Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) {
				Path newPath = Paths.get("${definition.buildDir.toAbsolutePath()}", "${file.fileName}")

				if (file.renameTo(newPath.toString())) {
					LoggingUtils.log("Moving new build to zipstore: $file.fileName", definition)
					moved++
				}
			}
		}

		// TODO: add warning if no builds have been found for 30 days
		if (moved > 0) {
			LoggingUtils.log("Moved $moved new build files", definition)
		} else {
			LoggingUtils.log("No new build files found in the inbox", definition)
		}
	}

	static void fetchAvailableBuilds(XamlDefinition definition) {
		// get all builds
		(Files.list(definition.buildDir)
			.findAll { Path path -> path.fileName.toString().toLowerCase().endsWith(".zip")
			} as List<Path>).each { Path archive ->
			LoggingUtils.log(String.format("Processing \"%s\"", archive.toAbsolutePath().toString()), definition)

			XamlBuild build = new XamlBuild(definition, archive)

			if (setBuildStatus(definition, build)) {
				// If the build status cannot be determined skip the build
				definition.builds.add(build)
			}
		}

		definition.builds.sort({ a, b -> a.getStartTime().compareTo(b.getStartTime()) })
	}

	/**
	 * Set the build status for the given build.
	 * The build status is defined if the errors.txt in the build files has entries or not.
	 */
	static boolean setBuildStatus(XamlDefinition definition, XamlBuild build) {
		List<Path> matches = ZipUtils.getMatches(build.archive, definition.config.errors)

		try {
			if (matches.size() != 1) {
				LoggingUtils.warn("Found ${matches.size()} matches for $definition.config.errors.pathPattern but " +
					"expected exactly one. Skipping the build")
				return false
			}
			def errorsFile = matches[0]

			if (errorsFile.text.length() > 0) {
				build.setResult(EBuildResult.FAILED)
			} else {
				build.setResult(EBuildResult.SUCCEEDED)
			}

			return true
		} finally {
			matches.forEach { Files.deleteIfExists(it) }
		}
	}

	protected static Path createZipstoreDirectory(String basePath, String projectName, String definitionName) {
		Path path = Paths.get(basePath, projectName, definitionName)

		if (!Files.exists(path)) {
			Files.createDirectories(path)
		}

		if (!Files.isDirectory(path)) {
			LoggingUtils.warn("Zipstore for $path is not a directory.")
		}

		return path
	}
}
