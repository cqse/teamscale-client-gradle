package com.teamscale.gradle.azureDevOps.extensions

class ProjectConfig extends NamedClosureContainer {
	String name

	Map<String, BuildDefinitionOptions> definitions = new HashMap<>()

	ProjectConfig(String name) {
		this.name = name
	}

	def get(String key) {
		return definitions.get(key)
	}

	@Override
	protected Object getNewInstance(String name) {
		definitions.put(name, new BuildDefinitionOptions(name))
		return definitions.get(name)
	}
}
