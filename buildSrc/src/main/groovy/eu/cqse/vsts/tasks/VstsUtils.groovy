package eu.cqse.vsts.tasks

import groovyx.net.http.HttpBuilder
import groovyx.net.http.HttpException
import groovyx.net.http.OkHttpBuilder

class VstsUtils {
	static HttpBuilder getHttpClient(url, credentials) {
		return OkHttpBuilder.configure {
			request.uri = url
			// Workaround for a HTTPBuilder Problem/Bug
//			teamscale.url = teamscale.url.replaceAll("/\$", "")
//			teamscale.prefix = new URL(teamscale.url).getPath() ?: ""

			request.headers['Authorization'] = "Basic " + "${credentials.username}:${credentials.password}".bytes.encodeBase64().toString()
		}
	}

	/** Custom failure handler for request. More verbose. */
	static failure = { resp, reader ->
		println "############################ Response ##################################"
		println resp.getUri()
		println reader
		println "########################################################################"
		throw new HttpException(resp, reader)
	}
}
