package com.teamscale.gradle.azureDevOps.extensions

class ProjectConfig extends NamedClosureContainer {
	String name

	Map<String, BuildDefinitionConfiguration> definitions = new HashMap<>()

	ProjectConfig(String name) {
		this.name = name
	}

	def get(String key) {
		return definitions.get(key)
	}

	@Override
	protected Object getNewInstance(String name) {
		definitions.put(name, new BuildDefinitionConfiguration(name))
		return definitions.get(name)
	}
}
