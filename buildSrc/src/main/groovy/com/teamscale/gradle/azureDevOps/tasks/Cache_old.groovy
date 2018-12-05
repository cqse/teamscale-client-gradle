package com.teamscale.gradle.azureDevOps.tasks

import com.teamscale.gradle.azureDevOps.config.EBuildInformationType
import com.teamscale.gradle.azureDevOps.data.Build
import com.teamscale.gradle.azureDevOps.data.Definition
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

import java.nio.file.Paths
import java.time.Instant

// TODO: rework O_O
class Cache_old {
	File cacheFile

	/**
	 * The actual cashing. The time needs to be a string because otherwise the Instant is being written as
	 *{ "epochSecond": 1542624386, "nano": 403015600 }, which cannot be parsed back to an Instant.
	 */
	// TODO: Check why the key cannot be EBuildInformationType
	Map<String, Map<String, String>> cache = new HashMap<>()

	private Cache_old(File cacheFile) {
		this.cacheFile = cacheFile
		if (cacheFile.exists()) {
			load()
		} else {
			save()
		}
	}

	private static String getCacheName(String projectName) {
		return "${projectName}.json"
	}

	static Cache_old getCacheFor(File cacheDir, String projectName) {
		File projectCache = Paths.get(cacheDir.getPath(), getCacheName(projectName)).toFile()
		return new Cache_old(projectCache)
	}

	private void load() {
		cache = new JsonSlurper().parseText(cacheFile.text)
	}

	private void save() {
		cacheFile.write(JsonOutput.prettyPrint(JsonOutput.toJson(cache)))
	}

	void set(Definition definition, EBuildInformationType option, Build build) {
		putForType(definition, option, getTime(build))
		save()
	}

	/**
	 * Checks whether the given build hasn't been processed yet with the given type
	 */
	boolean hasNotBeenProcessed(Definition definition, EBuildInformationType type, Build build) {
		Instant last = getForType(definition, type)
		if (!last) {
			return true
		}

		return getTime(build).compareTo(last) > 0
	}

	private Instant getForType(Definition definition, EBuildInformationType type) {
		String value = getOrCreate(definition).get(type.toString())

		if (!value) {
			return Instant.EPOCH
		} else {
			return Instant.parse(value)
		}
	}

	private void putForType(Definition definition, EBuildInformationType type, Instant time) {
		getOrCreate(definition).put(type.toString(), time.toString())
	}

	private getOrCreate(Definition definition) {
		String key = getKey(definition)
		if (!cache.containsKey(key)) {
			cache.put(key, getEmptyTypeMap())
		}
		return cache.get(key)
	}

	private static getEmptyTypeMap() {
		def map = new HashMap<String, String>()
		EBuildInformationType.values().each { EBuildInformationType type ->
			map.put(type.toString(), Instant.EPOCH.toString())
		}
		return map
	}

	Instant getMinTime(Definition definition) {
		Instant min = null

		EBuildInformationType.values().each { EBuildInformationType type ->
			def time = getForType(definition, type)
			if (!min) {
				min = time
			}

			if (min.compareTo(time) > 0) {
				min = time
			}
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
