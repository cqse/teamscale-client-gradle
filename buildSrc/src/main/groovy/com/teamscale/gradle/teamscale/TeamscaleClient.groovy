package com.teamscale.gradle.teamscale

import com.teamscale.gradle.teamscale.data.NonCodeMetric
import com.teamscale.gradle.teamscale.data.StandardQueryParameter
import com.teamscale.gradle.teamscale.data.TeamscaleConfig
import com.teamscale.gradle.teamscale.data.TeamscaleFinding
import groovyx.net.http.MultipartContent
import groovyx.net.http.OkHttpBuilder
import groovyx.net.http.OkHttpEncoders
import okhttp3.OkHttpClient

import static java.util.concurrent.TimeUnit.SECONDS

class TeamscaleClient extends HttpClient {
	final String prefix

	final TeamscaleConfig server

	/** If this flag is true the uploads to the teamscale server are mocked and always 'succeed' */
	boolean disableUpload = false

	/** The value which is returned when an upload to teamscale was successful */
	static final String UPLOAD_SUCCESS_RETURN = "success"

	static Closure acceptJson = { request ->
		request.accept = "application/json"
	}

	TeamscaleClient(TeamscaleConfig server) {
		super(createHttpClient(server))
		this.server = server
		this.prefix = new URL(server.url).getPath().replaceAll("^/", "") ?: null
	}

	/**
	 * Configures and creates the client which manages the connection to the teamscale server.
	 */
	static createHttpClient(TeamscaleConfig server) {
		assert server.accesskey != null: "No access key given"
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

	def disableUploads() {
		disableUpload = true
	}

	/**
	 * Makes an HTTP call to the teamscale server. Prepends any necessary prefix or subpath.
	 */
	protected Object doCall(String method, List<String> service, Map<String, String> query, setRequest = {}, boolean projectCall = true) {
		List<String> path = [prefix]
		if(projectCall) {
			path += ["p", server.project]
		}

		path = (path + service).findAll {
			it != null
		}

		if (disableUpload && method in ["post", "put"]) {
			return UPLOAD_SUCCESS_RETURN
		}

		return super.doCall(method, path, query, setRequest)
	}

	/**
	 * Calls a project service for teamscale. Prepends any necessary prefix or subpath.
	 */
	protected Object doProjectCall(String method, String service, Map<String, String> query, setRequest = {}) {
		return doCall(method, [service], query, setRequest, true)
	}

	/**
	 * Calls a global REST Api service call for teamscale.
	 */
	protected Object doGlobalCall(String method, String service, Map<String, String> query, setRequest = {}) {
		return doGlobalCall(method, [service], query, setRequest)
	}

	/**
	 * Calls a global REST Api service call for teamscale.
	 */
	protected Object doGlobalCall(String method, List<String> service, Map<String, String> query, setRequest = {}) {
		return doCall(method, service, query, setRequest, false)
	}


	/** Uploads the given build's result */
	String uploadNonCodeMetrics(StandardQueryParameter standard, List<NonCodeMetric> metrics) {
		def query = standard.asMap() + ["skip-session": "true"]

		def setBody = { request ->
			request.contentType = "application/json"
			request.body = metrics
		}

		return doProjectCall("put", "add-non-code-metrics", query, setBody)
	}

	/**
	 * Uploads the content of one or more external reports given in a list. The given type must be a valid for
	 * Teamscale.
	 */
	String uploadExternalReports(StandardQueryParameter standard, List<String> reports, String type,
								 Map<String, String> optionalParameter = [:]) {
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

		return doProjectCall("post", "external-report", query, setRequest)
	}

	/**
	 * Uploads the given findings to teamscale.
	 */
	String uploadExternalFindings(StandardQueryParameter standard, List<TeamscaleFinding> findings) {
		def query = standard.asMap() + ["skip-session": "true"]

		def setRequest = { request ->
			request.contentType = "application/json"
			request.body = findings.groupBy({ finding -> finding.path }).collect { k, v ->
				["path": k, "findings": v]
			}
		}

		return doProjectCall("put", "add-external-findings", query, setRequest)
	}

	List getExternalUploads() {
		return doProjectCall("get", "external-result-upload", [:], acceptJson) as List
	}

	List<String> getAllProjects() {
		return doGlobalCall("get", "projects", [:], acceptJson) as List
	}
}