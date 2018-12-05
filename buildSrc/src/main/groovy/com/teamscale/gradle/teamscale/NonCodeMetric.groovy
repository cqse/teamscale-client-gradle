package com.teamscale.gradle.teamscale

class NonCodeMetric {
	String path
	String content
	Long time
	Map<EAssessment, Integer> assessment = new HashMap<>()

	NonCodeMetric(String path, String content, Long time) {
		this.path = path
		this.content = content
		this.time = time
	}

	void addAssessment(EAssessment type, Integer value) {
		assessment.put(type, value)
	}
}
