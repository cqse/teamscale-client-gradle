package com.teamscale.gradle.azureDevOps.config

import com.teamscale.gradle.azureDevOps.data.Definition
import com.teamscale.gradle.azureDevOps.tasks.Cache
import com.teamscale.gradle.azureDevOps.utils.ELogAnalyzerType
import com.teamscale.gradle.azureDevOps.utils.ILogAnalyzer
import com.teamscale.gradle.azureDevOps.utils.LogAnalyzerFactory
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project

class AzureDevOps {
	public final static NAME = "azureDevOps"

	NamedDomainObjectContainer<OrganizationConfig> builds
	NamedDomainObjectContainer<Credentials> credentials

	ILogAnalyzer logAnalyzer

	List<Definition> definitions = new ArrayList<>()

	Cache cache

	Project project

	String codeCoverageExePath

	AzureDevOps(project) {
		builds = project.container(OrganizationConfig)
		credentials = project.container(Credentials)
		this.project = project
	}

	def builds(Closure closure) {
		builds.configure(closure)
	}

	def credentials(Closure closure) {
		credentials.configure(closure)
	}

	def logAnalyzer(String type) {
		def logAnalyzerType = ELogAnalyzerType.valueOf(type)
		logAnalyzer = LogAnalyzerFactory.getLogAnalyzer(logAnalyzerType)
	}

	def cacheDir(String path) {
		def cacheDir = new File(path)
		assert cacheDir.exists() && cacheDir.isDirectory():
			"Given cache directory $path does not exists or is not a directory"

		cache = Cache.getCacheFor(cacheDir, project.name)
	}

	def codeCoverageExe(String path) {
		def codeCoverage = new File(path)
		assert codeCoverage.exists() && codeCoverage.isFile():
			"The path to the code coverage exe does not point to a file: $path"
		codeCoverageExePath = path
	}
}