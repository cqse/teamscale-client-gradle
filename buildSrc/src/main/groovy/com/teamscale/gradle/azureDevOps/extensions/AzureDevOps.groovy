package com.teamscale.gradle.azureDevOps.extensions


import com.teamscale.gradle.azureDevOps.data.AdosDefinition
import com.teamscale.gradle.azureDevOps.utils.Cache
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project

/**
 * Gradle extension for the extensions related to the Azure dev ops services (ADOS) build processing.
 */
class AzureDevOps {
	public final static NAME = "azureDevOps"

	/**
	 * The configured definitions and its builds (named builds because it is more clear for the extensions)
	 */
	NamedDomainObjectContainer<OrganizationConfig> builds

	/**
	 * Credentials for the different organization
	 */
	NamedDomainObjectContainer<Credentials> credentials

	/**
	 * TODO
	 */
	XamlExtension xaml = new XamlExtension()

	/**
	 * The downloaded information about the configured definitions from ADOS.
	 */
	List<AdosDefinition> definitions = new ArrayList<>()

	/**
	 * Cache object for the project. This handles the saving of the times a build has last been processed
	 * for each upload task.
	 */
	Cache cache

	/**
	 * Reference to the gradle project.
	 */
	Project project

	/**
	 * The path to the .exe file which can convert .coverage files into .xml, which then can
	 * be uploaded to teamscale.
	 */
	String codeCoverageExePath

	AzureDevOps(project) {
		builds = project.container(OrganizationConfig)
		credentials = project.container(Credentials)
		this.project = project
	}

	/**
	 * Define definitions and its builds
	 */
	def builds(Closure closure) {
		builds.configure(closure)
	}

	/**
	 * Define credentials to the different ADOS organizations.
	 */
	def credentials(Closure closure) {
		credentials.configure(closure)
	}

	def xaml(Closure closure) {
		closure.delegate = xaml
		closure.resolveStrategy = Closure.DELEGATE_ONLY
		closure.run()
	}

	/**
	 * Set the cache for this project using the given cache directory.
	 * Checks if the directory does exist.
	 */
	def cacheDir(String path) {
		def cacheDir = new File(path)
		assert cacheDir.exists() && cacheDir.isDirectory():
			"Given cache directory $path does not exists or is not a directory"

		cache = Cache.getCacheFor(cacheDir, project.name)
	}

	/**
	 * Configure the location of the code coverage exe which converts `.coverage` files to `.xml`, which
	 * can be processed by TS
	 */
	def codeCoverageExe(String path) {
		def codeCoverage = new File(path)
		assert codeCoverage.exists() && codeCoverage.isFile():
			"The path to the code coverage exe does not point to a file: $path"
		codeCoverageExePath = path
	}
}