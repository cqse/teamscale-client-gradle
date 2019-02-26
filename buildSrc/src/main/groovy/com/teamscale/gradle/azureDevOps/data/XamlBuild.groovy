package com.teamscale.gradle.azureDevOps.data

import com.teamscale.gradle.azureDevOps.tasks.upload.UploadAdosBuildStatusTask

import java.nio.file.Path
import java.time.Instant
import java.util.zip.ZipFile

/**
 * Representing a XAML build, which are given as zip files.
 * Every information about the build must be parsed from files inside of the archive.
 * The status of the build can be determined by checking if an errors.txt file has content.
 */
class XamlBuild implements IBuild {
	String name
	String targetBranch
	Instant time
	String result
	Path archive

	XamlBuild(XamlDefinition definition, Path archive) {
		this.name = "$archive.fileName".substring(0, "$archive.fileName".length() - 4) // remove .zip from the end
		this.targetBranch = definition.config.branch
		this.time = getTimeOfBuildFromName("$archive.fileName", definition.getName())
		this.archive = archive
	}

	@Override
	Instant getStartTime() {
		return time
	}

	@Override
	Instant getFinishTime() {
		return time
	}

	@Override
	long getExecutionTime() {
		// TODO: find out if there is an execution time in the old build upload scripts
		return 0
	}

	@Override
	String getTeamscaleBranch() {
		return targetBranch
	}

	@Override
	String getName() {
		return name
	}

	@Override
	boolean hasFailed() {
		return result == UploadAdosBuildStatusTask.BUILD_FAILED
	}

	void setResult(String result) {
		this.result = result
	}

	/**
	 * The name of the archive file is always build as follows: "<name-of-definition>_yyyyMMdd_HH-
	 */
	static Instant getTimeOfBuildFromName(String fileName, String definitionName) {
		String dateString = fileName.replace(definitionName, "")
		dateString = dateString.toLowerCase().replace(".zip", "")

		return Date.parse("_yyyyMMdd_HH-mm-ss", dateString).toInstant()
	}
}
