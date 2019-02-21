package com.teamscale.gradle.azureDevOps.extensions

class Credentials {
	String name
	String username
	String password

	Credentials(String name) {
		this.name = name
	}
}
