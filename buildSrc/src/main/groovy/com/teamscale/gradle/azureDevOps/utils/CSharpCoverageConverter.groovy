package com.teamscale.gradle.azureDevOps.utils

import static com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils.warn

class CSharpCoverageConverter {
	static List<String> convert(List<File> files, String execPath) {
		File xml = File.createTempFile("coverage", ".xml")

		List<String> contents = new ArrayList<>()
		files.each { coverage ->
			def command = ["cmd", "/C", "\"\"$execPath\"", "analyze", "/output:\"$xml.absolutePath\"",
						   "\"$coverage.absolutePath\"\""].execute()
			def errorStream = new StringBuffer()
			command.waitForProcessOutput(null, errorStream)

			if(errorStream.size() > 0) {
				warn("Converting the coverage file failed: $errorStream")
			}

			// TODO: assert or cancel in any way?
			if(!xml.exists()) {
				warn("Convertion of the coverage file did not work. No output to $xml.absolute")
			}

			contents.add(xml.text)
			xml.delete()
		}

		return contents
	}
}
