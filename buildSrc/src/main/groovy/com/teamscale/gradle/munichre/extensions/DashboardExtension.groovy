package com.teamscale.gradle.munichre.extensions

class DashboardExtension {
	final static NAME = "dashboard"

	String template

	String data

	String tool

	String placeholder = "dummy"

	boolean projectMustExist = true
}