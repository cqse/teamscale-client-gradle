package com.teamscale.gradle.teamscale

import groovyx.net.http.HttpBuilder
import groovyx.net.http.HttpException

class HttpClient {
	final HttpBuilder http

	HttpClient(HttpBuilder http) {
		this.http = http
	}

	protected Object doCall(String method, List<String> pathParameters, Map<String, String> queryParameters, setRequest = {
	}) throws SocketTimeoutException {
		def result = http."$method" {
			setRequest.delegate = delegate
			setRequest(request)

			request.uri.useRawValues = true
			request.uri.path = "/" + encodeParams(pathParameters).join("/")
			request.uri.query = encodeQueryParams(queryParameters)

			response.failure(failure)
		}

		if (result instanceof byte[]) {
			return new String(result)
		}
		return result
	}

	private static encodeParams(List<String> params) {
		return params.collect { it -> URLEncoder.encode(it, "UTF-8").replaceAll("\\+", "%20") }
	}

	private static encodeQueryParams(Map<String, String> query) {
		return query.collectEntries { key, value ->
			[(key): URLEncoder.encode(value, "UTF-8").replaceAll("\\+", "%20")]
		} as Map<String, String>
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