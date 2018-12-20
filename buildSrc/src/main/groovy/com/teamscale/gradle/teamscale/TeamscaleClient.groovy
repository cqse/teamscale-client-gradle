package com.teamscale.gradle.teamscale

import groovyx.net.http.MultipartContent
import groovyx.net.http.OkHttpBuilder
import groovyx.net.http.OkHttpEncoders
import okhttp3.OkHttpClient

import static java.util.concurrent.TimeUnit.SECONDS

class TeamscaleClient extends HttpClient {
	final String prefix

	final TeamscaleConfig server

	/** The value which is returned when an upload to teamscale was successful */
	static final UPLOAD_SUCCESS_RETURN = "success"

	TeamscaleClient(TeamscaleConfig server) {
		super(createHttpClient(server))
		this.server = server
		this.prefix = new URL(server.url).getPath().replaceAll("^/", "") ?: null
	}

	/**
	 * Configures and creates the client which manages the connection to the teamscale server.
	 */
	static createHttpClient(TeamscaleConfig server) {
		assert server.accesskey != null: "No accesskey given"
		server.url = server.url.replaceAll("/\$", "")

		return OkHttpBuilder.configure {
			request.uri = server.url
			request.headers["Authorization"] = "Basic " + "${server.username}:${server.accesskey}".bytes.encodeBase64().toString()

			clientConfig.clientCustomizer { OkHttpClient.Builder builder ->
				builder.connectTimeout(server.timeout.connect, SECONDS)
				builder.writeTimeout(server.timeout.write, SECONDS)
				builder.readTimeout(server.timeout.read, SECONDS)
			}
		}
	}

	/**
	 * Makes an HTTP call to the teamscale server. Prepends any necessary prefix or subpath.
	 */
	protected Object doCall(String method, List<String> service, Map<String, String> query, setRequest = {}) {
		List<String> path = ([prefix, "p", server.project] + service).findAll {
			it != null
		}
		super.doCall(method, path, query, setRequest)
	}

	/** Uploads the given build's result */
	String uploadBuildStatus(StandardQueryParameter standard, List<NonCodeMetric> metrics) {
		def query = standard.asMap() + ["skip-session": "true"]

		def setBody = { request ->
			request.contentType = "application/json"
			request.body = metrics
		}

		return doCall("put", ["add-non-code-metrics"], query, setBody)
	}

	/**
	 * Uploads the content of one or more external reports given in a list. The given type must be a valid for
	 * Teamscale.
	 */
	String uploadExternalReports(StandardQueryParameter standard, List<String> reports, String type,
								 Map<String, String> optionalParameter = [:]) {
		if (reports.size() == 0) {
			// TODO: check if this makes sense
			// What should happen if no reports are given for the upload
			return "success"
		}

		def query = standard.asMap() + ["format": type] + optionalParameter

		def setRequest = { request ->
			request.contentType = "multipart/form-data"
			request.encoder "multipart/form-data", OkHttpEncoders.&multipart

			request.body = MultipartContent.multipart {
				reports.each {
					field "report", it
				}
			}
		}

		return doCall("post", ["external-report"], query, setRequest)
	}

	/**
	 * Uploads the given findings to teamscale.
	 */
	String uploadExternalFindings(StandardQueryParameter standard, List<TeamscaleFinding> findings) {
		def path = ["add-external-findings"]
		def query = standard.asMap() + ["skip-session": "true"]

		def setRequest = { request ->
			request.contentType = "application/json"
			request.body = findings.groupBy({ finding -> finding.path }).collect { k, v ->
				["path": k, "findings": v]
			}
		}

		return doCall("put", path, query, setRequest)
	}
}