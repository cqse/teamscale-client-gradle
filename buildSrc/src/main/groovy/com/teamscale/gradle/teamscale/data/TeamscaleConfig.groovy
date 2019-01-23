package com.teamscale.gradle.teamscale.data

class TeamscaleConfig {
	String project
	String url
	String username
	String accesskey

	def timeout = [
		connect: 60,
		write  : 60,
		read   : 60
	]

	String toString() {
		return "$project, $url, $username, $accesskey"
	}
}
