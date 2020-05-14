package com.teamscale.gradle.teamscale.tasks.dashboard

import com.teamscale.gradle.teamscale.TeamscaleClient
import groovyx.net.http.MultipartContent
import groovyx.net.http.OkHttpEncoders
import groovyx.net.http.optional.Download

import static com.teamscale.gradle.teamscale.TeamscaleClient.acceptJson

/** Utils for fetching and uploading dashboards to teamscale */
class DashboardUtils {
	/** Return the names of all dashboards */
	static List<String> getAllDashboards(TeamscaleClient http) {
		return http.doGlobalCall("get", "dashboards", [:], acceptJson) as List<String>
	}

	/** Get the export of the dashboard of the given name. */
	static Object getDashboard(TeamscaleClient http, String dashboardName) {
		File tmpFile = File.createTempFile("temp", ".tsdashboard")
		tmpFile.deleteOnExit()

		http.doGlobalCall("get", ["dashboard-export", dashboardName], [:], { request ->
			Download.toFile(delegate, tmpFile)
		})

		return tmpFile.text
	}

	/** Upload the given dashboard descriptor to teamscale */
	static void uploadDashboard(TeamscaleClient http, Object dashboard) {
		http.doGlobalCall("post", "dashboard-export", [:], { request ->
			request.encoder "multipart/form-data", OkHttpEncoders.&multipart
			request.contentType = "multipart/form-data"

			request.body = MultipartContent.multipart {
				field "dashboardDescriptor", dashboard.toString()
			}
		})
	}
}