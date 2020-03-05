package com.teamscale.gradle.azureDevOps.tasks.ados


import com.teamscale.gradle.azureDevOps.data.AdosBuild
import com.teamscale.gradle.azureDevOps.data.AdosDefinition
import com.teamscale.gradle.azureDevOps.tasks.EBuildInformationType
import com.teamscale.gradle.azureDevOps.tasks.base.UploadTask
import com.teamscale.gradle.azureDevOps.utils.AdosUtils
import com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils
import com.teamscale.gradle.teamscale.TeamscaleClient
import com.teamscale.gradle.teamscale.data.TeamscaleExtension
import groovyx.net.http.MultipartContent
import groovyx.net.http.OkHttpEncoders

/**
 * Fetch pdb from a build artifact und upload it to teamscale
 */
class UploadAdosPDBs extends UploadTask<AdosDefinition, AdosBuild> {
	static String TASK_NAME = "uploadPDBs"

	private final static int MAX_PDB_UPLOAD = 8

	@Override
	void run(AdosDefinition definition, AdosBuild build) {
		String version = getVersionNumber(definition, build)
		if(!version) {
			return
		}

		List<File> pdbs = AdosUtils.getFilesFromBuildArtifact(definition, build, definition.options.pdb.pdbMatcher)
		if(pdbs.size() == 0) {
			LoggingUtils.warn("No pdbs found for $definition.options.pdb.pdbMatcher", definition, build)
			return
		}

		String timestamp = build.getStartTime().toEpochMilli().toString()

		// Upload the pdbs in batches
		String result = null
		int numberOfPdbs = 0
		LoggingUtils.log("Uploading ${pdbs.size()} PDBs for version $version ($timestamp)", definition, build)
		pdbs.collate(MAX_PDB_UPLOAD).each { pdbSlice ->
			numberOfPdbs += pdbSlice.size()
			result = uploadPDBs(version, timestamp, pdbSlice)
			if(result == TeamscaleClient.UPLOAD_SUCCESS_RETURN) {
				LoggingUtils.log("Uploaded $numberOfPdbs/${pdbs.size()}", definition, build)
			} else {
				LoggingUtils.warn("Upload failed", definition, build)
				return
			}
		}
		processUploadResult(definition,build, result, "Success")
	}

	/**
	 * Upload the given pdbs to teamscale
	 */
	String uploadPDBs(String version, timestamp, List<File> pdbs) {
		def setRequest = { request ->
			request.contentType = 'multipart/form-data'
			request.body = MultipartContent.multipart {
				pdbs.each {
					part 'file', it.name, 'text/plain', it
				}
			}
			request.encoder 'multipart/form-data', OkHttpEncoders.&multipart
		}

		def query = [
			"t": timestamp.toString(),
			"version": version
		]

		return getTeamscaleClient().doProjectCall("post", "dotnet-debug-info-upload", query, setRequest)
	}

	/**
	 * Get the product version number from a dll in the artifact which is selected by the version matcher.
	 * This can only be done on windows as it requires a powershell.
	 */
	static String getVersionNumber(AdosDefinition definition, AdosBuild build) {
		def matcher = definition.options.pdb.versionMatcher
		def matched = AdosUtils.getFilesFromBuildArtifact(definition, build, matcher)

		if(matched.size() == 0) {
			return null
		}

		if(matched.size() > 1) {
			LoggingUtils.warn("Found multiple artifact matches for $matcher. Taking first", definition, build)
		}

		def dll = matched[0]
		def powerShellCommand = "[System.Diagnostics.FileVersionInfo]::GetVersionInfo('$dll.absolutePath').ProductVersion"
		def shellCommand = "powershell.exe -ExecutionPolicy Bypass -NoLogo -NonInteractive -NoProfile -Command \"${powerShellCommand}\""
		def process = shellCommand.execute()
		def out = new StringBuffer()
		def err = new StringBuffer()
		process.consumeProcessOutput(out, err)
		process.waitFor()

		def error = err.toString()
		if (error.size() > 0) {
			LoggingUtils.warn("Error during fetching the Product version from the matched dll with $matcher: $error")
			return null
		}

		return out.toString().trim()
	}

	@Override
	List<AdosDefinition> getDefinitions() {
		return TeamscaleExtension.getFrom(project).azureDevOps.definitions
	}

	@Override
	String getRejectReason() {
		return "No PDB configuration"
	}

	@Override
	EBuildInformationType getUploadType() {
		return EBuildInformationType.BUILD_PDBS
	}

	@Override
	boolean isConfiguredForTask(AdosDefinition definition) {
		return definition.options.pdb?.versionMatcher && definition.options.pdb?.pdbMatcher
	}

	@Override
	protected String getDefaultPartition() {
		return ""
	}
}
