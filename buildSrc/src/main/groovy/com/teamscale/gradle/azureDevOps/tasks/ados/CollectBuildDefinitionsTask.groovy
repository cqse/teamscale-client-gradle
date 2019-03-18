package com.teamscale.gradle.azureDevOps.tasks.ados

import com.teamscale.gradle.azureDevOps.client.AzureDevOpsClient
import com.teamscale.gradle.azureDevOps.extensions.AzureDevOpsExtension
import com.teamscale.gradle.azureDevOps.extensions.OrganizationConfig
import com.teamscale.gradle.azureDevOps.extensions.ProjectConfig
import com.teamscale.gradle.azureDevOps.data.AdosDefinition
import com.teamscale.gradle.teamscale.data.TeamscaleExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils.log

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
				def http = new AzureDevOpsClient(credentials, organization.name, projectConfig.name)

				// minimize the api calls and get all definitions at once and sort then
				def definitions = http.getAllDefinitions().findResults { data ->
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
				diff.removeAll((List<String>) definitions.name)
				diff.removeAll((List<String>) definitions.id)
				assert diff.size() == 0: "No definition(s) found on \"$organization.name/$projectConfig.name\" with " +
					"the following name(s): " + diff

				definitions.each { AdosDefinition definition ->
					azureDevOps.definitions.add(definition)

					def lastCompleted = "No builds yet"
					if(definition.lastCompletedTime) {
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
}
