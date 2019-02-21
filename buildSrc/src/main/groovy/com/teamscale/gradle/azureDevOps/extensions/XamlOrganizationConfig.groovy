package com.teamscale.gradle.azureDevOps.extensions

class XamlOrganizationConfig extends NamedClosureContainer {
	String name

	Map<String, XamlProjectConfig> projects = new HashMap<>()

	XamlOrganizationConfig(String name) {
		this.name = name
	}

	@Override
	protected Object getNewInstance(String name) {
		projects.put(name, new XamlProjectConfig(name))
		return projects.get(name)
	}
}
