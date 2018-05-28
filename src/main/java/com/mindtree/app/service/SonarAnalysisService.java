package com.mindtree.app.service;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.annotation.PostConstruct;

import org.apache.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.aig.dcp.cmn.log.Logger;
import com.aig.dcp.cmn.utils.Utils;
import com.mindtree.app.constants.SonarAnalysisConstants;
import com.mindtree.app.dao.SonarAnalysisDaoI;

@Service
public class SonarAnalysisService implements SonarAnalysisI {

	@Autowired
	private SonarAnalysisDaoI sonarAnalysisdao;

	private static final Logger logger = Logger.getLogger(SonarAnalysisService.class);
	private static final int BUFFER_SIZE = 4096;

	@PostConstruct
	public void verify() {
		logger.info("sonarAnalysisdao Implementation injected successfully");
	}

	public String extractZipFileToDir(MultipartFile fileData) {
		String destDirectory = "";
		String savedProjectPath = "";
		try (ZipInputStream zipInputStream = new ZipInputStream(fileData.getInputStream())) {
			if (!Utils.isEmpty(fileData)) {
				ZipEntry zipEntry = zipInputStream.getNextEntry();
				destDirectory = SonarAnalysisConstants.PROJECT_UPLOAD_DIR;
				zipInputStream.getNextEntry();
				while (zipEntry != null) {
					String filePath = destDirectory + File.separator + zipEntry.getName();
					if (zipEntry.isDirectory()) {
						File dir = new File(filePath);
						dir.mkdir();
					} else {
						extractFile(zipInputStream, filePath);
					}
					zipInputStream.closeEntry();
					zipEntry = zipInputStream.getNextEntry();
				}
				savedProjectPath = destDirectory
						+ fileData.getOriginalFilename().substring(0, fileData.getOriginalFilename().lastIndexOf('.'));
			} else {
				logger.info("File Data is empty ,Exiting!!");
			}
		} catch (IOException e) {
			logger.error("IO Exception" + e);
		}
		return savedProjectPath;
	}

	public void extractFile(ZipInputStream zipInputStream, String filePath) {
		try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));) {
			byte[] bytesIn = new byte[BUFFER_SIZE];
			int read = 0;
			while ((read = zipInputStream.read(bytesIn)) != -1) {
				bos.write(bytesIn, 0, read);
			}
		} catch (IOException e) {
			logger.error("IO Exception");
		}
	}

	@Override
	public HttpResponse getAnalysisReport(String projectKey) {
		return sonarAnalysisdao.getResultsFromSonar(projectKey);
	}

	@Override
	public HttpResponse runAnalysis(String directoryPath, String fileName) {
		return sonarAnalysisdao.runSonarAnalysisOfJavaProject(directoryPath, fileName);
//		return sonarAnalysisdao.getResultsFromSonar(projectKey);
	}

	public HttpResponse checkProjectExists(String projectKey) {
		return sonarAnalysisdao.checkProjectExists(projectKey);

	}

	public String processResponse(HttpResponse response) {
		if(response==null){
			logger.error("Response is null.");
			return null;
		}
		StringBuilder builder = new StringBuilder();
		try {
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(response.getEntity().getContent()));
			String line = "";

			while ((line = bufferedReader.readLine()) != null) {
				builder.append(line);
			}
		} catch (IOException e) {
			logger.error("Exception ocurred while reading the sonar analysis json response results " + e);
		}
		return builder.toString();
	}

	public boolean checkComponentExists(String processedResponse) {
		boolean isProjectExists = false;
		try {
			JSONObject jsonObject = new JSONObject(processedResponse);
			if (jsonObject.has("component")) {
				isProjectExists = true;
			} else {
				isProjectExists = false;
			}
		} catch (JSONException e) {
			logger.error(
					"Exception ocurred while processing the response json ,got when checking if projoct exists " + e);
		}
		return isProjectExists;
	}
}
