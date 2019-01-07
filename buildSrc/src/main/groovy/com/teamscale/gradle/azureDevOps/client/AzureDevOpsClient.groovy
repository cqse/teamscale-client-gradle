package com.teamscale.gradle.azureDevOps.client

import com.teamscale.gradle.azureDevOps.config.Credentials
import com.teamscale.gradle.teamscale.HttpClient
import groovyx.net.http.OkHttpBuilder
import groovyx.net.http.optional.Download

import java.time.Instant

/**
 * Client dealing with service calls to the azure dev ops services.
 * The client is configured to a project in a so-called organization (for example 'apps-munichre') for
 * convenience.
 */
class AzureDevOpsClient extends HttpClient {
	final static URL = "https://dev.azure.com/"

	final String project

	final String organization

	final Map<String, String> defaultQueryParameters = [
		"api-version": "5.0-preview"
	]

	AzureDevOpsClient(Credentials credentials, String organization, String project) {
		super(OkHttpBuilder.configure {
			request.uri = this.URL
			request.headers['Authorization'] = "Basic " +
				"${credentials.username}:${credentials.password}".bytes.encodeBase64().toString()
		})
		this.organization = organization
		this.project = project
	}

	/**
	 * Makes the request to the configured azure dev ops service server.
	 * The return type depends on the request.
	 * Take a look at the online API for all possible REST calls:
	 * https://docs.microsoft.com/en-us/rest/api/azure/devops/?view=azure-devops-rest-5.0
	 */
	protected Object doCall(String method, List<String> path, Map<String, String> query, closure = {}) {
		path = [organization, project, "_apis"] + path
		query = defaultQueryParameters + query
		return super.doCall(method, path, query, closure)
	}

	/**
	 * Returns all definitions for the project this client is configured for.
	 */
	List getAllDefinitions() {
		return doCall("get", ["build", "definitions"], ["includeLatestBuilds": "true"]).value as List
	}

	/**
	 * Returns all current and retained builds for the given definition id. If a minTime is provided it will
	 * only return the builds which finished AFTER the given Instant.
	 */
	List getBuildsForDefinition(String definitionId, Instant minTime = null) {
		def parameters = [
			"definitions": definitionId,
			"status"     : "completed",
			"queryOrder" : "finishTimeAscending"
		]

		if (minTime) {
			parameters.put("minTime", minTime.toString())
		}

		return doCall("get", ["build", "builds"], parameters).value as List
	}

	/**
	 * Downloads the test coverage for the given build id.
	 * If the build does not have coverage, an emtpy list is returned.
	 */
	List<File> downloadTestCoverage(String buildId) {
		// Get URLs
		def path = ["test", "codecoverage"]
		def query = ["buildId": buildId, "flags": "2"]
		List<String> coverageFileUrls = doCall("get", path, query).value.codeCoverageFileUrl

		List<File> coverageFiles = downloadFiles(coverageFileUrls)

		return coverageFiles
	}

	/**
	 * Downloads the files from the given urls and returns a list of file handles.
	 * The urls must be at least have the same organization if they are at the azure dev ops server.
	 */
	List<File> downloadFiles(List<String> urls) {
		List<File> files = new ArrayList<>()
		urls.each { url ->
			files.add((File) http.get {
				request.uri = url
				Download.toTempFile(delegate)
				response.failure(failure)
			})
		}
		return files
	}

	/**
	 * Returns all test runs for this build.
	 * IMPORTANT: This also contains any possible test runs for any release based on this build.
	 * If you only want the runs for the actual building, than filter for those runs which do not have
	 * a `release` property.
	 */
	Object getTestRunsForBuild(String buildUri) {
		def path = ["test", "runs"]
		def query = ["buildUri": buildUri, "includeRunDetails": "true"]
		return doCall("get", path, query).value
	}

	/** Returns a list of the attachments for the given test run */
	Object getAttachmentsOfTestRun(Integer runId) {
		def path = ["test", "runs", "$runId", "attachments"]
		return doCall("get", path, [:]).value
	}

	/** Returns the timeline for the build defined by the given id */
	Object getTimelineOfBuild(String buildId) {
		def path = ["build", "builds", buildId, "timeline"]
		return doCall("get", path, [:]).records
	}

	/** Returns the logs for the build defined by the given id */
	Object getLogsOfBuild(String buildId) {
		def path = ["build", "builds", buildId, "logs"]
		return doCall("get", path, [:]).value
	}

	Object downloadLog(String buildId, String logId, int startLine, int endLine) {
		def path = ["build", "builds", buildId, "logs", "$logId"]
		def query = ["startLine": "$startLine", "endLine": "$endLine"]
		return doCall("get", path, query)
	}

	Object getArtifacts(String buildId) {
		def path = ["build", "builds", buildId, "artifacts"]
		return doCall("get", path, [:]).value
	}

	/**
	 * Returns a description for the contents of an artifacts. This enables the downloading of specific files from
	 * a published artifact without having to download the whole archive.
	 *
	 * The given Object must be a json of the BuildArtifact format:
	 * https://docs.microsoft.com/en-us/rest/api/azure/devops/build/artifacts/list?view=azure-devops-rest-5.0#buildartifact
	 */
	Object getArtifactContents(Object artifactDescription) {
		List<String> artifactDataTokens = artifactDescription.resource.data.tokenize("/")

		if (artifactDataTokens.size() <= 1) {
			return null
		}

		// The data 'normally' looks similar to this '#/<container-id>/<folder-name>'
		String containerId = artifactDataTokens.get(1)

		String artifactFolderName = null
		if (artifactDataTokens.size() > 2) {
			artifactFolderName = artifactDataTokens.subList(2, artifactDataTokens.size()).join("/")
		}

		// project mustn't be in the URL
		def path = [organization, "_apis", "resources", "Containers", containerId]
		def query = defaultQueryParameters + [
			"itemPath": artifactFolderName
		]

		return super.doCall("get", path, query, {}).value
	}
}
