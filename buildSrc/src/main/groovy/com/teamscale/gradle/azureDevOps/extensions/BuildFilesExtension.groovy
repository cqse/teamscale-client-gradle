package com.teamscale.gradle.azureDevOps.extensions

import com.teamscale.gradle.azureDevOps.data.XamlDefinition

class BuildFilesExtension extends NamedClosureContainer {
	Map<String, BuildFilesOrganizationConfig> organizations = [:]
	List<XamlDefinition> definitions = []

	@Override
	protected Object getNewInstance(String name) {
		organizations.put(name, new BuildFilesOrganizationConfig(name))
		return organizations.get(name)
	}
}
