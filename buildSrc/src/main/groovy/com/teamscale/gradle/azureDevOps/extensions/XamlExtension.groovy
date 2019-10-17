package com.teamscale.gradle.azureDevOps.extensions

import com.teamscale.gradle.azureDevOps.data.XamlDefinition

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class XamlExtension extends NamedClosureContainer {
	Map<String, XamlOrganizationConfig> organizations = [:]

	int maxStoredZips

	/** Directory of the inbox */
	Path inbox

	/** Directory of the zip store */
	Path zipStore

	List<XamlDefinition> definitions = []

	def inbox(String path) {
		inbox = Paths.get(path)
		assert Files.isDirectory(inbox): "inbox directory '$path' does not exist or is not a directory"
	}

	def zipstore(String path) {
		zipStore = Paths.get(path)
		assert Files.isDirectory(zipStore): "zip store directory '$path' does not exist or is not a directory"
	}

	@Override
	protected Object getNewInstance(String name) {
		organizations.put(name, new XamlOrganizationConfig(name))
		return organizations.get(name)
	}
}
