package com.teamscale.gradle.azureDevOps.utils

import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class ZipUtils {

	/**
	 * Searches for and returns all matches for the given patterns in the archive as temporary files.
	 * The filenames and the paths inside of the archives are not preserved.
	 */
	static List<Path> getMatches(Path archive, ReportLocationMatcher matcher) {
		ZipFile file = new ZipFile(archive.toAbsolutePath().toString(), Charset.forName("CP437"))
		try {
			return (file.entries() as List<ZipEntry>).findResults { ZipEntry entry ->
				if (matcher.pathMatches(entry.name)) {
					return extractFile(file, entry.name)
				}
			}
		} finally {
			file.close()
		}
	}

	/**
	 * Extract the file defined by the given path from the archive
	 */
	private static Path extractFile(ZipFile archive, String path) {
		ZipEntry entry = archive.getEntry(path)
		Path tmp = Files.createTempFile("temp", ".tmp")
		if (entry) {
			tmp << archive.getInputStream(entry).bytes
			return tmp
		}
		return null
	}

	/**
	 * Searches for and returns all matches for the given patterns in the archive. Only the matching files are
	 * extracted and returned in a temporary folder. The paths of the files inside of the archive are preserved.
	 */
	static Path getMatchesPreservePaths(Path archive, ReportLocationMatcher matcher) {
		Path tempDir = Files.createTempDirectory("coverage")
		ZipFile file = new ZipFile(archive.toAbsolutePath().toString(), Charset.forName("CP437"))
		try {

			(file.entries() as List<ZipEntry>).each { entry ->
				if (matcher.pathMatches(entry.name)) {
					extractFileToFolderPreservePath(file, tempDir, entry.name)
				}
			}

			return tempDir
		} finally {
			file.close()
		}
	}

	/**
	 * Extract the file specified by the path in the archive to the folder while preserving the folder structure where
	 * the file is located inside of the archive.
	 * This is not secure for
	 */
	static void extractFileToFolderPreservePath(ZipFile archive, Path destFolder, String path) {
		Path extracted = Paths.get(destFolder.toAbsolutePath().toString(), path)
		Files.createDirectories(extracted.getParent())
		ZipEntry entry = archive.getEntry(path)

		if (entry) {
			extracted << archive.getInputStream(entry).bytes
		}
	}
}
