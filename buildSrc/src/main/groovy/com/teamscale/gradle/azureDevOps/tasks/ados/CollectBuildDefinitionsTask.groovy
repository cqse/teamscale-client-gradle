package com.teamscale.gradle.azureDevOps.tasks.ados

import com.teamscale.gradle.azureDevOps.client.AzureDevOpsClient
import com.teamscale.gradle.azureDevOps.data.AdosDefinition
import com.teamscale.gradle.azureDevOps.extensions.AzureDevOpsExtension
import com.teamscale.gradle.azureDevOps.extensions.OrganizationConfig
import com.teamscale.gradle.azureDevOps.extensions.ProjectConfig
import com.teamscale.gradle.teamscale.data.TeamscaleExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils.log
import static com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils.warn

/**
 * Collect the information about all defined build definitions.
 */
class CollectBuildDefinitionsTask extends DefaultTask {
	static String TASK_NAME = "collectBuildDefinitions"

	@TaskAction
	void collect() {
		AzureDevOpsExtension azureDevOps = TeamscaleExtension.getFrom(project).azureDevOps

		azureDevOps.builds.each { OrganizationConfig organization ->
			def credentials = azureDevOps.credentials[organization.name]

			organization.projects.values().each { ProjectConfig projectConfig ->
				AzureDevOpsClient http = new AzureDevOpsClient(credentials, organization.name, projectConfig.name)

				// minimize the api calls and get all definitions at once and sort then
				def definitions = getAllDefinitions(http, organization, projectConfig)

				definitions.each { AdosDefinition definition ->
					azureDevOps.definitions.add(definition)

					def lastCompleted = "No builds yet"
					if (definition.lastCompletedTime) {
						lastCompleted = "$definition.lastCompletedTime"
					}

					log("$definition.name [$lastCompleted]")

					if (definitions.size() > 1) {
						assert definition.options.partition: "The project '$projectConfig.name' " +
							"has ${definitions.size()} definitions, but there is no partition defined for " +
							"'$definition.name ($definition.id)'.\nIf there are multiple definitions for a project, " +
							"each one has to have a 'partition' property in its extensions gradle-file"
					}
				}
			}
		}
	}

	/**
	 * Fetches the information for all configured definitions
	 */
	List<AdosDefinition> getAllDefinitions(AzureDevOpsClient http, OrganizationConfig organizationConfig, ProjectConfig projectConfig) {
		AzureDevOpsExtension azureDevOps = TeamscaleExtension.getFrom(project).azureDevOps

		List<AdosDefinition> allDefinitions
		try {
			allDefinitions = http.getAllDefinitions();
		} catch (RuntimeException e) {
			if (e.getCause() instanceof SocketTimeoutException) {
				warn("Time-out while fetching all definitions for '$projectConfig.name' at '$organizationConfig.name'")
				return []
			}
			throw e
		}

		List<AdosDefinition> matchingDefinitions = allDefinitions.findResults { data ->
			def nameMatches = projectConfig.definitions.containsKey(data.name)
			def idMatches = projectConfig.definitions.containsKey(data.id.toString())

			if (idMatches) {
				return new AdosDefinition(projectConfig.get(data.id.toString()), http, data, azureDevOps.cache)
			}

			if (nameMatches) {
				return new AdosDefinition(projectConfig.get(data.name.toString()), http, data, azureDevOps.cache)
			}
		}

		// Check that every definition is found
		def diff = new HashSet(projectConfig.definitions.keySet())
		diff.removeAll((List<String>) matchingDefinitions.name)
		diff.removeAll((List<String>) matchingDefinitions.id)
		assert diff.size() == 0: "No definition(s) found on \"$organizationConfig.name/$projectConfig.name\" with " +
			"the following name(s): " + diff

		return matchingDefinitions
	}
}
