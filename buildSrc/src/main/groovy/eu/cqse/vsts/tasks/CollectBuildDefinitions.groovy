package eu.cqse.vsts.tasks

import eu.cqse.vsts.config.VstsPluginExtension;
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.time.Instant

class CollectBuildDefinitions extends DefaultTask {
	@TaskAction
	void collect() {
		project.vsts.config.builds.each { vstsUrl, vstsProjects ->
			vstsProjects.each {vstsProjectName, vstsBuilds ->

//				def http = VstsUtils.getHttpClient(vstsUrl, config.credentials.get(vstsUrl))
//
//				def response = http.get {
//					request.uri.path = "/$vstsProjectName/_apis/build/definitions"
//					request.uri.query = [
//						'api-version'        : '4.1',
//						'includeLatestBuilds': true
//					]
//					response.failure(VstsUtils.failure)
//				}
//
//				def definitions = response.value.findAll { vstsBuildDefinition ->
//					return vstsBuilds.containsKey(vstsBuildDefinition.name)
//				}
//
//				// Check that every definition name is valid
//				def diff = new HashSet(vstsBuilds.keySet())
//				diff.removeAll(definitions.collect {it.name})
//				assert diff.size() == 0: "No definition(s) found on \"$vstsUrl/$vstsProjectName\" with " +
//					"the following name(s): " + diff
//
//				definitions.each { vstsBuildDefinition ->
//					if (!vstsBuilds.containsKey(vstsBuildDefinition.name)) {
//						return
//					}
//
//					def lastCompletedTime = Instant.EPOCH
//					if (!vstsBuildDefinition.latestCompletedBuild) {
//						log("No build run/completed for $vstsBuildDefinition.name")
//					} else {
//						lastCompletedTime = Instant.parse(vstsBuildDefinition.latestCompletedBuild.finishTime)
//					}
//
//					def buildDefinitionKey = [
//						url    : vstsUrl,
//						project: vstsProjectName,
//						name   : vstsBuildDefinition.name,
//						options: defaultOptions + vstsBuilds[vstsBuildDefinition.name]
//					]
//
//					buildDefinitions[buildDefinitionKey] = [
//						id               : vstsBuildDefinition.id,
//						lastCompletedTime: lastCompletedTime,
//						http             : http,
//						builds           : []
//					]
//
//					if (definitions.size() > 1) {
//						assert buildDefinitionKey.options.partition: "The project '$buildDefinitionKey.project' " +
//							"has ${definitions.size()} definitions, but there is no partition defined for " +
//							"'$buildDefinitionKey.name'.\nIf there are multiple definitions for a project, " +
//							"each one has to have a 'partition' property in its config gradle-file"
//					}
//				}
			}
		}
	}
}
