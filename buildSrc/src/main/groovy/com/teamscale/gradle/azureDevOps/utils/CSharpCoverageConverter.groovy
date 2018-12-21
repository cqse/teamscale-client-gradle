package com.teamscale.gradle.azureDevOps.utils

import static com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils.warn

class CSharpCoverageConverter {
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
