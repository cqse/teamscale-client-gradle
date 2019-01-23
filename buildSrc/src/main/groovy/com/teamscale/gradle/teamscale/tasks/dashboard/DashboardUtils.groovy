package com.teamscale.gradle.teamscale.tasks.dashboard

import com.teamscale.gradle.teamscale.TeamscaleClient

import static com.teamscale.gradle.teamscale.TeamscaleClient.acceptJson

class DashboardUtils {
	static List<String> getAllDashboards(TeamscaleClient http) {
		return http.doGlobalCall("get", "dashboards", [:], acceptJson) as List<String>
	}

	static Object getDashboard(TeamscaleClient http, String dashboardName) {
		return http.doGlobalCall("get", ["dashboards", dashboardName], [:], acceptJson)
	}
}
