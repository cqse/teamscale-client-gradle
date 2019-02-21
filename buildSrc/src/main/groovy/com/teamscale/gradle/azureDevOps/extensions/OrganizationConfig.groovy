package com.teamscale.gradle.azureDevOps.extensions

class OrganizationConfig extends NamedClosureContainer {
	String name

	Map<String, ProjectConfig> projects = new HashMap<>()

	OrganizationConfig(String name) {
		this.name = name
	}

	@Override
	protected Object getNewInstance(String name) {
		projects.put(name, new ProjectConfig(name))
		return projects.get(name)
	}
}
