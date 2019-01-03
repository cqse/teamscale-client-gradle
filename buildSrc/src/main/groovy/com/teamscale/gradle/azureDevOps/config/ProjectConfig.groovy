package com.teamscale.gradle.azureDevOps.config

class ProjectConfig extends NamedClosureContainer {
	String name

	Map<String, DefinitionOptions> definitions = new HashMap<>()

	ProjectConfig(String name) {
		this.name = name
	}

	def get(String key) {
		return definitions.get(key)
	}

	@Override
	protected Object getNewInstance(String name) {
		definitions.put(name, new DefinitionOptions(name))
		return definitions.get(name)
	}
}
