package com.teamscale.gradle.azureDevOps.tasks.ados

import com.teamscale.gradle.azureDevOps.client.AzureDevOpsClient
import com.teamscale.gradle.azureDevOps.data.AdosBuild
import com.teamscale.gradle.azureDevOps.data.AdosDefinition
import com.teamscale.gradle.azureDevOps.tasks.EBuildInformationType
import com.teamscale.gradle.azureDevOps.tasks.base.UploadTask
import com.teamscale.gradle.azureDevOps.utils.AdosUtils
import com.teamscale.gradle.azureDevOps.utils.logging.LoggingUtils
import com.teamscale.gradle.teamscale.data.TeamscaleExtension

/**
 * Collect the information about all defined build definitions.
 */
class UploadAdosPDBs extends UploadTask<AdosDefinition, AdosBuild> {
	static String TASK_NAME = "uploadPDBs"

	@Override
	void run(AdosDefinition definition, AdosBuild build) {
		String version = getVersionNumber(definition, build)
		if(!version) {
			return
		}
	}

	String getVersionNumber(AdosDefinition definition, AdosBuild build) {
		def matcher = definition.options.pdb.versionConfig
		def matched = AdosUtils.getFilesFromBuildArtifact(definition, build, matcher)

		if(matched.size() == 0) {
			return null
		}

		if(matched.size() > 0) {
			LoggingUtils.warn("Found multiple artifact matches for $matcher. Taking first", definition, build)
		}

		def dll = matched[0]
		def powerShellCommand = "[System.Diagnostics.FileVersionInfo]::GetVersionInfo($dll.absolutePath).ProductVersion"
		def shellCommand = "powershell.exe -ExecutionPolicy Bypass -NoLogo -NonInteractive -NoProfile -Command \"${powerShellCommand}\""
		def process = shellCommand.execute()
		process.waitFor()
		def versionNumber = process.outputStream
		println versionNumber
		return versionNumber
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
		return definition.options.pdb.versionConfig && definition.options.pdb.pdbConfig
	}

	@Override
	protected String getDefaultPartition() {
		return ""
	}
}
