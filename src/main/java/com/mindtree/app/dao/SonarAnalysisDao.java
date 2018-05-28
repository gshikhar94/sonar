package com.mindtree.app.dao;

import java.io.IOException;
import java.util.Properties;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.sonarsource.scanner.api.EmbeddedScanner;
import org.sonarsource.scanner.api.LogOutput;
import org.springframework.stereotype.Repository;

import com.aig.dcp.cmn.log.Logger;
import com.mindtree.app.constants.SonarAnalysisConstants;

/**
 * @author SGupta3
 *
 */
@Repository
public class SonarAnalysisDao implements SonarAnalysisDaoI {
	private static final Logger logger = Logger.getLogger(SonarAnalysisDao.class);
	public static HttpResponse analysisResponse = null;

	public HttpResponse runSonarAnalysisOfJavaProject(String projectPath, String fileName) {

		Threading thread = new Threading(projectPath, fileName);
		Thread t1 = new Thread(thread);
		t1.start();
		try {
			// Wait for this thread to end ,then only proceed forward
			t1.join();
		} catch (InterruptedException e) {
			logger.error("Exception ocurred while making the delay to process the results" + e);

			// Restore interrupted state...
			Thread.currentThread().interrupt();
		}
		logger.info("Analysis response we got is " + analysisResponse);
		return analysisResponse;
	}

	public HttpResponse getResultsFromSonar(String projectKey) {
		HttpClient client = HttpClientBuilder.create().build();
		String url = "http://localhost:9000/api/issues/search?componentKeys=" + projectKey + "&format=json";
		HttpGet httpGet = new HttpGet(url);
		HttpResponse response = null;
		try {
			logger.info("Halting the process for 10 seconds for project having projectKey [" + projectKey + "]");
			Thread.sleep(10000);
			response = client.execute(httpGet);
			logger.info("Fetching response got completed for the project with key [" + projectKey + "]");
		} catch (ClientProtocolException e) {
			logger.error(
					"Exception ocurred while communication with sonar api for generating the analysis results " + e);
		} catch (InterruptedException e) {
			logger.error("Exception ocurred while making the delay to process the results" + e);

			// Restore interrupted state...
			Thread.currentThread().interrupt();
		} catch (IOException e) {
			logger.error("Exception ocurred while analyzing project having key [" + projectKey + "]" + e);
		}
		return response;
	}

	public HttpResponse checkProjectExists(String projectKey) {
		HttpClient client = HttpClientBuilder.create().build();
		String url = "http://localhost:9000/api/components/show?component=" + projectKey + "&format=json";
		HttpGet httpGet = new HttpGet(url);
		HttpResponse response = null;
		try {
			response = client.execute(httpGet);
			logger.info("Fetching response got completed for the project with key [" + projectKey + "]");
		} catch (ClientProtocolException e) {
			logger.error(
					"Exception ocurred while communication with sonar api for generating the analysis results " + e);
		} catch (IOException e) {
			logger.error("Exception ocurred while analyzing project having key [" + projectKey + "]" + e);
		}

		return response;

	}

}

class Threading extends SonarAnalysisDao implements Runnable, LogOutput {
	private String savedProjectPath;
	private String fileName;

	public Threading(String savedProjectPath, String fileName) {
		this.savedProjectPath = savedProjectPath;
		this.fileName = fileName;
	}

	public Threading() {

	}

	private static final Logger logger = Logger.getLogger(Threading.class);

	@Override
	public void run() {
		logger.info("Project Path is " + savedProjectPath);
		Properties allProps = new Properties();
		String projectKey = fileName.substring(0, 4);
		allProps.put(SonarAnalysisConstants.PROJECT_BASEDIR, savedProjectPath);
		allProps.put(SonarAnalysisConstants.PROJECT_SOURCES, savedProjectPath + "/src/main/java");
		allProps.put(SonarAnalysisConstants.PROJECT_BINARIES, savedProjectPath + "/src/");
		allProps.put(SonarAnalysisConstants.PROJECT_NAME, fileName);
		allProps.put(SonarAnalysisConstants.PROJECT_KEY, projectKey);

		try {
			allProps.load(getClass().getClassLoader().getResourceAsStream("sonar-project.properties"));
		} catch (IOException e) {
			logger.error("Exception ocurred while running the analysis for a particular project" + e);
		}
		EmbeddedScanner scanner = EmbeddedScanner.create(new Threading()).addGlobalProperties(allProps)
				.unmask("org.apache.tools.ant").unmask("org.sonar.ant");

		scanner.start();

		logger.info("Running Analysis for the project [" + fileName + "]");

		scanner.runAnalysis(allProps);
		logger.info("Analysis got completed for the project [" + fileName + "]");
		scanner.stop();

		SonarAnalysisDao dao = new SonarAnalysisDao();
		HttpResponse response = dao.getResultsFromSonar(projectKey);
		SonarAnalysisDao.analysisResponse = response;
	}

	@Override
	public void log(String formattedMessage, Level level) {
//
	}

}
