package com.teamscale.gradle.teamscale.data

class UtilsExtension {
	List<Mute> mute = []

	def mute(String partition, String message) {
		mute.add(new Mute(partition: partition, message: message))
	}

	static class Mute {
		String partition
		String message
	}
}
