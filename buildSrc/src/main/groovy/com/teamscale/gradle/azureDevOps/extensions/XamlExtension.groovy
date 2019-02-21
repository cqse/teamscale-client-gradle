package com.teamscale.gradle.azureDevOps.extensions

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

// TODO: check if organizations are actually useful.

class XamlExtension extends NamedClosureContainer {
	Map<String, XamlOrganizationConfig> organizations = [:]

	int maxZips

	/** Directory of the inbox */
	Path inbox

	/** Directory of the zip store */
	Path zipStore

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
