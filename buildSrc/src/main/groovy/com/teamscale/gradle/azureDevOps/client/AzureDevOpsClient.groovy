package com.teamscale.gradle.azureDevOps.client

import com.teamscale.gradle.azureDevOps.config.Credentials
import com.teamscale.gradle.teamscale.HttpClient
import groovyx.net.http.OkHttpBuilder

import java.time.Instant

import static com.teamscale.gradle.azureDevOps.utils.Logging.log

class AzureDevOpsClient extends HttpClient {
	final static URL = "https://dev.azure.com/test"

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
	@Override
	private Object doCall(String method, List<String> path, Map<String, String> query, closure = {}) {
		path = [organization, project, "_apis"] + path
		query = defaultQueryParameters + query
		return super.doCall(method, path, query, closure)
	}

	/**
	 * Returns all definitions for the project this client is configured for.
	 */
	Map<String, String> getAllDefinitions() {
		return doCall("get", ["build", "definitions"], ["includeLatestBuilds": "true"])
	}

	/**
	 * Returns all current and retained builds for the given definition id. If a minTime is provided it will
	 * only return the builds which finished AFTER the given Instant.
	 */
	Map<String, String> getBuildsForDefinition(String definitionId, Instant minTime = null) {
		def parameters = [
			"definitions": definitionId,
			"status"     : "completed",
			"queryOrder" : "finishTimeAscending"
		]

		if (minTime) {
			parameters.put("minTime", minTime.toString())
		}

		return doCall("get", ["build", "builds"], parameters)
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

		// Download the files
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
				groovyx.net.http.optional.Download.toTempFile(delegate)
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
		return doCall("get", path, query)
	}

	/** Returns a list of the attachments for the given test run */
	Object getAttachmentsOfTestRun(Integer runId) {
		def path = ["test", "runs", "$runId", "attachments"]
		return doCall("get", path, [:])
	}

	/** Returns the timeline for the build defined by the given id */
	Object getTimelineOfBuild(String buildId) {
		def path = ["build", "builds", buildId, "timeline"]
		return doCall("get", path, [:])
	}

	/** Returns the logs for the build defined by the given id */
	Object getLogsOfBuild(String buildId) {
		def path = ["build", "builds", buildId, "logs"]
		return doCall("get", path, [:])
	}

	Object downloadLog(String buildId, String logId, int startLine, int endLine) {
		def path = ["build", "builds", buildId, "logs", "$logId"]
		def query = ["startLine": "$startLine","endLine": "$endLine"]
		return doCall("get", path, query)
	}
}
