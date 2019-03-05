package com.teamscale.gradle.azureDevOps.utils.convert

import com.teamscale.gradle.azureDevOps.utils.AzureBuildException

import java.nio.file.Files
import java.nio.file.Path

class MSCoverageConverter {
	static List<String> convert(List<File> files, String execPath) {
		Path coverageDir = Files.createTempDirectory("coverage")
		Path xml = Files.createTempFile("coverage", ".xml")

		try {
			// copy coverage files to directory
			files.each {
				Files.createTempFile(coverageDir, "tmp", ".coverage") << it
			}

			def command = ["cmd", "/C", "\"\"$execPath\"",
						   "-d", "\"$coverageDir\"",
						   "-o", "\"$xml\"", "\""].execute()

			def errorStream = new StringBuffer()
			command.waitForProcessOutput(null, errorStream)

			if (errorStream.size() > 0) {
				throw new AzureBuildException("Converting the coverage file failed: $errorStream")
			}

			if (xml.text.size() == 0) {
				throw new AzureBuildException("Conversion of the coverage file resulted in an empty file")
			}

			return [xml.text]
		} finally {
			coverageDir.eachFile { it.delete() }
			Files.delete(coverageDir)
			Files.delete(xml)
		}
	}
}