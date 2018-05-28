package com.mindtree.app.service;

import java.util.zip.ZipInputStream;

import org.apache.http.HttpResponse;
import org.springframework.web.multipart.MultipartFile;

public interface SonarAnalysisI {

	/**
	 * @param fileData
	 * @return
	 */
	public String extractZipFileToDir(MultipartFile fileData);

	/**
	 * @param zipInputStream
	 * @param filePath
	 */
	public void extractFile(ZipInputStream zipInputStream, String filePath);

	/**
	 * @param projectId
	 * @return
	 */
	public HttpResponse getAnalysisReport(String projectId);

	/**
	 * @param directoryPath
	 * @param fileName
	 * @return
	 */
	public HttpResponse runAnalysis(String directoryPath, String fileName);

	/**
	 * @param response
	 * @return
	 */
	public String processResponse(HttpResponse response);

	/**
	 * @return
	 */
	public boolean checkComponentExists(String processedResponse);

	/**
	 * @param projectKey
	 * @return
	 */
	public HttpResponse checkProjectExists(String projectKey);
}
