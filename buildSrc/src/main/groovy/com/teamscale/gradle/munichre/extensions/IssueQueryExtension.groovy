package com.teamscale.gradle.munichre.extensions

class IssueQueryExtension {
	final static NAME = "issue_queries"

	/**
	 * Issue query for fetching issues which have been closed between now and the given amount of time.
	 * String can be formatted by providing one parameter which depicts the maximum amount of time a ticket was closed.
	 */
	private static final String ISSUE_QUERY_CLOSED = "" +
		"('Work Item Type' != Task and closed=true and inState(closed=true) < %1\$s) or " +
		"('Work Item Type' = Task and hasParent(closed=true and inState(closed=true) < %1\$s)) or " +
		"('Work Item Type' = Task and parent = '' and closed=true and inState(closed=true) < %1\$s)"

	public Map<String, String> queries = new HashMap<>()

	void closed(String name, String amountOfTime) {
		queries.put(name, String.format(ISSUE_QUERY_CLOSED, amountOfTime))
	}
}
