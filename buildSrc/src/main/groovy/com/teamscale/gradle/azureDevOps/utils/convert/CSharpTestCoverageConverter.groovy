package com.teamscale.gradle.azureDevOps.utils.convert

import static com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils.warn

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
class CSharpTestCoverageConverter {

	/**
	 *  Convert the given files using the provided executable.
	 */
	static List<String> convert(List<File> files, String execPath) {
		List<String> contents = new ArrayList<>()
		files.each { coverage ->
			File xml = new File(String.format("%s.xml", coverage.absolutePath))

			// tmp file must not exist before writing to it! Otherwise the command will not write an output
			if(xml.exists()) {
				xml.delete()
			}

			try {
				def command = ["cmd", "/C", "\"\"$execPath\"", "analyze", "/output:\"$xml.absolutePath\"",
							   "\"$coverage.absolutePath\"\""].execute()
				def errorStream = new StringBuffer()
				command.waitForProcessOutput(null, errorStream)

				if(errorStream.size() > 0) {
					warn("Converting the coverage file failed: $errorStream")
				}

				// TODO: assert or cancel in any way?
				if(!xml.exists() || xml.text.size() == 0) {
					warn("Convertion of the coverage file did not work. The converted file does not exist or" +
						"is empty.")
				}

				contents.add(xml.text)
			} finally {
				// Delete the downloaded files and prevent the clogging of the tmp dir
				if(xml.exists()) {
					xml.delete()
				}

				if(coverage.exists()) {
					coverage.delete()
				}
			}
		}

		return contents
	}


}
