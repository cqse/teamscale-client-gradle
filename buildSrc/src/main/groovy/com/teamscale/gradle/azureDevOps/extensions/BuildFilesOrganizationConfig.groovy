package com.teamscale.gradle.azureDevOps.extensions

class BuildFilesOrganizationConfig extends NamedClosureContainer {
	String name

	Map<String, XamlProjectConfig> projects = new HashMap<>()

	BuildFilesOrganizationConfig(String name) {
		this.name = name
	}

	@Override
	protected Object getNewInstance(String name) {
		projects.put(name, new XamlProjectConfig(name))
		return projects.get(name)
	}
}
