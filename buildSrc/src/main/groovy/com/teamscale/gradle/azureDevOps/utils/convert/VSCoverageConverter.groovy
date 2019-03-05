package com.teamscale.gradle.azureDevOps.utils.convert

import com.teamscale.gradle.azureDevOps.utils.AzureBuildException

import java.nio.file.Files
import java.nio.file.Path

/**
 * Converts any VS_COVERAGE -- normally with the file extension .coverage -- to an XML which can
 * be processed by teamscale.
 *
 * In order for it to work, a code coverage exe must be provided.
 * See https://github.com/danielpalme/ReportGenerator/wiki/Visual-Studio-Coverage-Tools for more information
 *
 * """
 * CodeCoverage.exe is another coverage tool that comes with Visual Studio 2012/2013 (Premium and Ultimate).
 * By default CodeCoverage.exe creates a *.coverage file. To generate a coverage report with ReportGenerator
 * the file has to be converted to *.xml format.
 * """
 */
class VSCoverageConverter {
	static List<String> convert(List<File> files, String execPath) {
		Path xml = Files.createTempFile("tmp", ".xml")

		try {
			// file musn't exist, otherwise nothing will be written to it
			if (xml.exists()) {
				xml.delete()
			}

			List<String> coverageFiles = files.collect { "\"$it.absolutePath\"" as String }
			def command = (["cmd", "/C", "\"\"$execPath\"", "analyze", "/output:\"$xml.absolutePath\""]
				+ coverageFiles
				+ ["\""]).execute()

			def errorStream = new StringBuffer()
			command.waitForProcessOutput(null, errorStream)

			if (errorStream.size() > 0) {
				throw new AzureBuildException("Converting the coverage file failed: $errorStream")
			}

			if (!xml.exists() || xml.text.size() == 0) {
				throw new AzureBuildException("Conversion of the coverage file did not work. " +
					"Consider using MS_COVERAGE instead, because the coverage files might be in an older format.")
			}

			return [xml.text]
		} finally {
			// prevent clogging of the temp dir
			if (xml.exists()) {
				xml.delete()
			}
		}
	}
}
