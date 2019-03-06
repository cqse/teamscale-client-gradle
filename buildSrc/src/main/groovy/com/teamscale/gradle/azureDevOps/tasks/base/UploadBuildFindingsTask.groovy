package com.teamscale.gradle.azureDevOps.tasks.base

import com.teamscale.gradle.azureDevOps.data.IBuild
import com.teamscale.gradle.azureDevOps.data.IDefinition
import com.teamscale.gradle.azureDevOps.tasks.EBuildInformationType
import com.teamscale.gradle.azureDevOps.utils.loganalyzer.ILogAnalyzer
import com.teamscale.gradle.teamscale.data.TeamscaleFinding

/**
 * Base class for uploading the build findings of a build from Azure DevOps Services to teamscale.
 * A build finding is e.g. a warning from a third-party tool checking the code for anomalies during the build process.
 */
abstract class UploadBuildFindingsTask<S extends IDefinition, T extends IBuild> extends UploadTask<S, T> {

	/** Maximum number of lines of a log which will be downloaded at once */
	public static final String PARTITION = "Build Findings"
	public static final String REJECT_REASON = "No log analyzer or log name pattern defined"

	/** Upload the findings to teamscale */
	void upload(S definition, T build, Set<TeamscaleFinding> findings) {
		def params = getStandardQueryParameters(definition, build, getDefaultPartition())
		def result = getTeamscaleClient().uploadExternalFindings(params, new ArrayList<>(findings))

		processUploadResult(definition, build, result, "Uploading ${findings.size()} finding(s): $result")
	}

	/** Parses any findings from the given string */
	static Set<TeamscaleFinding> parseLog(String content, ILogAnalyzer logAnalyzer) {
		Set<TeamscaleFinding> findings = []
		content.eachLine { String line ->
			TeamscaleFinding finding = logAnalyzer.analyze(line)
			if (finding) {
				findings.add(finding)
			}
		}
		return findings
	}

	@Override
	EBuildInformationType getUploadType() {
		EBuildInformationType.BUILD_FINDINGS
	}

	@Override
	protected String getDefaultPartition() {
		return PARTITION
	}

	@Override
	String getRejectReason() {
		return REJECT_REASON
	}
}