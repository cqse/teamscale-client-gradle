package com.teamscale.gradle.azureDevOps.utils.convert

import com.teamscale.gradle.azureDevOps.utils.AzureBuildException

import java.nio.file.Files
import java.nio.file.Path

/**
 * Converts the binary coverage-files from VS test runs.
 * The files will be converted into the MS_COVERAGE format which can be uploaded to teamscale.
 */
class MSCoverageConverter {

	public static final String TMP_NAME = "tmp_ms_cov"

	/**
	 * Fetches and converts all coverage files inside of the given folder.
	 * It is important that the folder does not only contain the coverage files but also dlls which are needed for
	 * the conversion process.
	 *
	 * Returns an xml file which can be uploaded to teamscale as a MS_COVERAGE report.
	 */
	static Path convert(Path folder, String execPath) {
		try {
			Path xml = Files.createTempFile(TMP_NAME, ".xml")
			// copy coverage files to directory
			def command = ["cmd", "/C", "\"\"$execPath\"",
						   "-d", "\"$folder\"",
						   "-o", "\"$xml\"", "\""].execute()

			def errorStream = new StringBuffer()
			command.waitForProcessOutput(null, errorStream)

			if (errorStream.size() > 0) {
				throw new AzureBuildException("Converting the coverage file failed: $errorStream")
			}

			if (xml.text.size() == 0) {
				throw new AzureBuildException("Conversion of the coverage file resulted in an empty file")
			}

			return xml
		} finally {
			folder.deleteDir()
		}
	}
}