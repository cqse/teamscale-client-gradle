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

	/** If true the cache will be empty and will not be written to a file */
	boolean disabled = false

	private Cache(File cacheFile) {
		this.cacheFile = cacheFile
		if (cacheFile.exists()) {
			load()
		}
	}

	/** Returns the name of the cache file based on the name of the project, e.g. "TQE" on apps-munichre */
	private static String getCacheFileName(String projectName) {
		return "${projectName}.json"
	}

	/** Returns a cache instance for the given project name. */
	static Cache getCacheFor(File cacheDir, String projectName) {
		File projectCache = Paths.get(cacheDir.getPath(), getCacheFileName(projectName)).toFile()
		return new Cache(projectCache)
	}

	/** Load the cache from file */
	private void load() {
		Map<String, Map<String, String>> loadedMap = new JsonSlurper().parseText(cacheFile.text) as Map
		this.cache = loadedMap.collectEntries { outer ->
			[outer.key, outer.value.collectEntries { inner ->
				[EBuildInformationType.valueOf(inner.key), Instant.parse(inner.value)]
			}]
		} as Map
	}

	/** Saves the current cache to file */
	private void save() {
		def saveMap = this.cache.collectEntries { outer ->
			[outer.key, outer.value.collectEntries { inner ->
				[inner.key.toString(), inner.value.toString()]
			}]
		}

		if (!disabled) {
			cacheFile.write(JsonOutput.prettyPrint(JsonOutput.toJson(saveMap)))
		}
	}

	/** Disables the cache. This means no cache will be written */
	void disable() {
		disabled = true
		cache = [:]
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

	/** Returns the last processed timestamp for the given definition and build information type */
	private Instant get(Definition definition, EBuildInformationType type) {
		return cache.get(getKey(definition), new HashMap<>()).getOrDefault(type, Instant.EPOCH)
	}

	/** Sets the last processed timestamp for the given definition and build information type */
	void set(Definition definition, EBuildInformationType type, Build build) {
		cache.get(getKey(definition), new HashMap<>()).put(type, getTime(build))
		save()
	}

	/**
	 * Returns the earliest timestamp for the given types. If there is no timestamp for a given type than
	 * the minimum is always Instant.EPOCH
	 */
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

		if (!min) {
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
