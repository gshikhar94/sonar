package com.mindtree.app.dao;

import org.apache.http.HttpResponse;

public interface SonarAnalysisDaoI {

	/**
	 * @param projectPath
	 * @param fileName
	 * @return
	 */
	public HttpResponse runSonarAnalysisOfJavaProject(String projectPath, String fileName);
	
	/**
	 * @param projectKey
	 * @return
	 */
	public HttpResponse getResultsFromSonar(String projectKey);
	
	/**
	 * @param projectKey
	 * @return
	 */
	public HttpResponse checkProjectExists(String projectKey);
}
