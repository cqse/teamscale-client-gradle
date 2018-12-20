package com.teamscale.gradle.teamscale

import groovyx.net.http.HttpBuilder
import groovyx.net.http.HttpException

class HttpClient {
	final HttpBuilder http

	HttpClient(HttpBuilder http) {
		this.http = http
	}

	private Object doCall(String method, List<String> pathParameters, Map<String, String> queryParameters, setRequest = {}) {
		def result = http."$method" {
			request.uri.path = "/" + pathParameters.join("/")
			request.uri.query = queryParameters
			setRequest(request)

			response.failure(failure)
		}

		if (result instanceof byte[]) {
			return new String(result)
		}
		return result
	}

	/** Custom failure handler for request. More verbose. */
	static failure = { resp, reader ->
		println "############################ Response ##################################"
		println resp.getUri()
		if (reader instanceof byte[]) {
			println new String(reader)
		} else {
			println reader
		}
		println "########################################################################"
		throw new HttpException(resp, reader)
	}
}