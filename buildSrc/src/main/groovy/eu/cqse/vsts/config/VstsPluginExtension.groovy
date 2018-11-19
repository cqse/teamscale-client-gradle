package eu.cqse.vsts.config

import org.gradle.api.Project
import org.gradle.api.provider.PropertyState
import org.gradle.api.provider.Provider

class VstsPluginExtension {
	TeamscaleConfig teamscale

	Map<URL, VstsCredentials> credentials

	BuildConfigs builds

	class BuildConfigs extends HashMap<URL, Projects> {}
	class Projects extends HashMap<String, Definitions> {}
	class Definitions extends HashMap<String, BuildOptions> {}
}

class VstsCredentials {
	String type
	String username
	String password
}