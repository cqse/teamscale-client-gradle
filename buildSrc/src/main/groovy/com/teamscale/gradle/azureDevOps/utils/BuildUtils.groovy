package com.teamscale.gradle.azureDevOps.utils

import com.teamscale.gradle.azureDevOps.config.TypeAndPattern
import com.teamscale.gradle.azureDevOps.data.Build
import com.teamscale.gradle.azureDevOps.data.Definition

class BuildUtils {
	/**
	 * Downloads the all files matching the artifact- and file-pattern in the given options.
	 * The artifactPattern in the given options must be set!
	 */
	static List<File> getFilesFromBuildArtifact(Definition definition, Build build, TypeAndPattern options) {
		List<File> coverageFiles = new ArrayList<>()

		assert options.mustSearchInArtifact(): "options must have an artifact pattern here! Is probably a missing" +
			"check in the code."

		List<Object> artifacts = definition.http.getArtifacts(build.id).value.findAll { artifact ->
			options.artifactMatches(artifact.name)
		}

		artifacts.each { artifact ->
			definition.http.getArtifactContents(artifact).value.each { item ->
				if(item.itemType == "file" && options.pathMatches(item.path)) {
					coverageFiles.addAll(definition.http.downloadFiles([item.contentLocation]))
				}
			}
		}

		return coverageFiles
	}
}
