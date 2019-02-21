package com.teamscale.gradle.azureDevOps.utils

import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class ZipUtils {

	/**
	 * Searches for and returns all matches for the given patterns in the archive.
	 */
	static List<Path> getMatchesInArchive(ZipFile file, ReportLocationMatcher matcher) {
		return (file.entries() as List<ZipEntry>).findResults { ZipEntry entry ->
			if (matcher.pathMatches(entry.name)) {
				return getFile(file, entry.name)
			}
		}
	}

	/**
	 * Extract the file defined by the given path from the archive
	 */
	private static Path getFile(ZipFile archive, String path) {
		ZipEntry entry = archive.getEntry(path)
		Path tmp = Files.createTempFile("temp", ".tmp")
		if (entry) {
			tmp << archive.getInputStream(entry).text
			return tmp
		}
		return null
	}
}
