package com.teamscale.gradle

import com.teamscale.gradle.azureDevOps.config.AzureDevOps
import com.teamscale.gradle.azureDevOps.tasks.CollectBuildDefinitionsTask
import com.teamscale.gradle.azureDevOps.tasks.CollectNewBuildsTask
import com.teamscale.gradle.azureDevOps.tasks.UploadTestCoverageTask
import com.teamscale.gradle.azureDevOps.tasks.UploadBuildFindingsTasks
import com.teamscale.gradle.azureDevOps.tasks.UploadBuildStatusTask
import com.teamscale.gradle.azureDevOps.tasks.UploadTestResultsTask
import com.teamscale.gradle.azureDevOps.utils.CustomEventLogger
import com.teamscale.gradle.teamscale.TeamscaleExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class TeamscalePlugin implements Plugin<Project> {
	@Override
	void apply(Project project) {
		project.getGradle().useLogger(new CustomEventLogger())

		def teamscale = project.extensions.create(TeamscaleExtension.NAME, TeamscaleExtension)
		teamscale[AzureDevOps.NAME] = new AzureDevOps(project)

		createAzureDevOpsTasks(project)
	}

	static createAzureDevOpsTasks(Project project) {
		def collectDefinitions = project.tasks.create("collectBuildDefinitions", CollectBuildDefinitionsTask)
		def collectNewBuilds = project.tasks.create("collectNewBuilds", CollectNewBuildsTask)
		collectNewBuilds.dependsOn(collectDefinitions)

		def buildStatus =project.tasks.create(UploadBuildStatusTask.NAME, UploadBuildStatusTask).dependsOn(collectNewBuilds)
		def testResults = project.tasks.create(UploadTestResultsTask.NAME, UploadTestResultsTask).dependsOn(collectNewBuilds)
		def testCoverage = project.tasks.create(UploadTestCoverageTask.NAME, UploadTestCoverageTask).dependsOn(collectNewBuilds)
		def buildFindings = project.tasks.create(UploadBuildFindingsTasks.NAME, UploadBuildFindingsTasks).dependsOn(collectNewBuilds)

		project.tasks.create("uploadBuildInformation").dependsOn(buildStatus, buildFindings, testResults, testCoverage)

		project.afterEvaluate {
			assert project.teamscale.azureDevOps.cache != null: "No cache set. Please use the 'cacheDir <path>` " +
				"inside of $AzureDevOps.NAME {}"
		}
	}
}
