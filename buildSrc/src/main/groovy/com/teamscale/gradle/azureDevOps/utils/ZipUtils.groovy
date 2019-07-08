package com.teamscale.gradle.azureDevOps.utils

import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class ZipUtils {

	public static final String TMP_NAME = "temp_"

	/**
	 * Searches for and returns all matches for the given patterns in the archive as temporary files.
	 * The filenames and the paths inside of the archives are not preserved.
	 */
	static List<Path> getMatches(Path archive, ReportLocationMatcher matcher) {
		ZipFile file = new ZipFile(archive.toAbsolutePath().toString(), Charset.forName("CP437"))
		try {
			return (file.entries() as List<ZipEntry>).findResults { ZipEntry entry ->
				if (matcher.pathMatches(entry.name)) {
					return extractFile(file, entry.name, archive.fileName.toString())
				}
			}
		} finally {
			file.close()
		}
	}

	/**
	 * Extract the file defined by the given path from the archive
	 */
	private static Path extractFile(ZipFile archive, String path, String archiveName) {
		ZipEntry entry = archive.getEntry(path)
		Path tmp = Files.createTempFile(TMP_NAME + archiveName, ".tmp")
		if (entry) {
			tmp << archive.getInputStream(entry).bytes
			return tmp
		}
		return null
	}

	/**
	 * Searches for and returns all matches for the given patterns in the archive. Only the matching files are
	 * extracted and returned in a temporary folder. The names of the files inside of the archive are preserved.
	 * However, files with the same name as already extracted files will not be extracted.
	 */
	static Path getMatchesPreserveNames(Path archive, ReportLocationMatcher matcher) {
		Path tempDir = Files.createTempDirectory("coverage")
		ZipFile file = new ZipFile(archive.toAbsolutePath().toString(), Charset.forName("CP437"))
		try {
			(file.entries() as List<ZipEntry>).each { entry ->
				if (matcher.pathMatches(entry.name)) {
					extractFileToFolderPreserveName(file, tempDir, entry.name)
				}
			}

			return tempDir
		} finally {
			file.close()
		}
	}

	/**
	 * Extract the file specified by the path in the archive to the folder while preserving the name of the file.
	 * If a file with that name already exists at the given location it will be skipped.
	 */
	static void extractFileToFolderPreserveName(ZipFile archive, Path destFolder, String path) {
		String name = Paths.get(path).getFileName().toString()
		Path extracted = Paths.get(destFolder.toAbsolutePath().toString(), name)

		if(extracted.toFile().exists()) {
			return
		}

		ZipEntry entry = archive.getEntry(path)
		if (entry) {
			extracted << archive.getInputStream(entry).bytes
		}
	}
}
