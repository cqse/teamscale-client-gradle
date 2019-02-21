package com.teamscale.gradle.azureDevOps.extensions

class XamlProjectConfig extends NamedClosureContainer {
	String name

	Map<String, XamlDefinitionOptions> definitions = new HashMap<>()

	XamlProjectConfig(String name) {
		this.name = name
	}

	def get(String key) {
		return definitions.get(key)
	}

	@Override
	protected Object getNewInstance(String name) {
		definitions.put(name, new XamlDefinitionOptions(name))
		return definitions.get(name)
	}
}
