package com.teamscale.gradle.azureDevOps.tasks

import com.teamscale.gradle.azureDevOps.data.Build
import com.teamscale.gradle.azureDevOps.data.Definition
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

import java.nio.file.Paths
import java.time.Instant

class Cache {
	File cacheFile

	Map<String, Map<EBuildInformationType, Instant>> cache = new HashMap<>()

	private Cache(File cacheFile) {
		this.cacheFile = cacheFile
		if (cacheFile.exists()) {
			load()
		}
	}

	private static String getCacheName(String projectName) {
		return "${projectName}.json"
	}

	static Cache getCacheFor(File cacheDir, String projectName) {
		File projectCache = Paths.get(cacheDir.getPath(), getCacheName(projectName)).toFile()
		return new Cache(projectCache)
	}

	private void load() {
		Map<String, Map<String, String>> loadedMap = new JsonSlurper().parseText(cacheFile.text)
		this.cache = loadedMap.collectEntries { outer ->
			[outer.key, outer.value.collectEntries { inner ->
				[EBuildInformationType.valueOf(inner.key), Instant.parse(inner.value)]
			}]
		}
	}

	private void save() {
		def saveMap = this.cache.collectEntries { outer ->
			[outer.key, outer.value.collectEntries { inner ->
				[inner.key.toString(), inner.value.toString()]
			}]
		}

		cacheFile.write(JsonOutput.prettyPrint(JsonOutput.toJson(saveMap)))
	}

	/**
	 * Checks whether the given build hasn't been processed yet with the given type
	 */
	boolean hasNotBeenProcessed(Definition definition, EBuildInformationType type, Build build) {
		Instant last = get(definition, type)
		if (!last) {
			return true
		}

		return getTime(build).compareTo(last) > 0
	}

	private Instant get(Definition definition, EBuildInformationType type) {
		return cache.get(getKey(definition), new HashMap<>()).getOrDefault(type, Instant.EPOCH)
	}

	void set(Definition definition, EBuildInformationType type, Build build) {
		cache.get(getKey(definition), new HashMap<>()).put(type, getTime(build))
		save()
	}

	Instant getMinTime(Definition definition, List<EBuildInformationType> types) {
		Instant min = null

		types.each { EBuildInformationType type ->
			Instant time = get(definition, type)
			if (!min) {
				min = time
			}

			if (min > time) {
				min = time
			}
		}

		if(!min) {
			return Instant.EPOCH
		}

		return min
	}

	private static Instant getTime(Build build) {
		return build.finishTime
	}

	private static String getKey(Definition definition) {
		return "${definition.getOrganization()}/${definition.getProject()}/$definition.name"
	}
}
