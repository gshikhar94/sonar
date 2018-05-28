package com.mindtree.app.controller;

import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.aig.dcp.cmn.utils.Utils;
import com.mindtree.app.service.SonarAnalysisService;

/**
 * @author SGupta3
 *
 */
@RestController
public class SonarAnalysisController {

	@Autowired
	private SonarAnalysisService sonarAnalysisService;
	private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	/**
	 * @Desc
	 *       <p>
	 *       This method is to check whether the service dependency is injected
	 *       successfully in this class
	 *       </p>
	 * @Author SGupta3 on Jan 17, 2017
	 */
	@PostConstruct
	public void verify() {
		logger.info("sonarAnalysisService Implementation injected successfully");
	}

	/**
	 * @param response
	 * @param fileData
	 * @return
	 */
	@Consumes({ MediaType.MULTIPART_FORM_DATA, MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	@RequestMapping(value = "/runAnalysis", method = RequestMethod.POST)
	public @ResponseBody String runAnalysis(
			@RequestParam(value = "fileData", required = false) MultipartFile fileData) {

		if (Utils.isEmpty(fileData)) {
			logger.warning("Exiting.No File Data found in the coming request");
			return "No file Data found";
		}
		HttpResponse response;
		String directoryPath = sonarAnalysisService.extractZipFileToDir(fileData);
		String fileNameWithoutExtension = fileData.getOriginalFilename().substring(0,
				fileData.getOriginalFilename().lastIndexOf('.'));
		String projectKey = fileNameWithoutExtension.substring(0, 4);

		HttpResponse projectExistsResponse = sonarAnalysisService.checkProjectExists(projectKey);
		String processedResponse = sonarAnalysisService.processResponse(projectExistsResponse);
		boolean isProjectExists = sonarAnalysisService.checkComponentExists(processedResponse);

		if (!isProjectExists) {
			logger.info("[" + fileNameWithoutExtension + "] Project does not exists .Running analysis...");
			response = sonarAnalysisService.runAnalysis(directoryPath, fileNameWithoutExtension);
			return sonarAnalysisService.processResponse(response);
			// return "Analysis got completed for the project
			// ["+fileNameWithoutExtension+"]";
		} else {
			logger.info("[" + fileNameWithoutExtension
					+ "] Project already exists .Not running analysis .Returning previous analyzed results");
			response = sonarAnalysisService.getAnalysisReport(projectKey);
			return sonarAnalysisService.processResponse(response);
		}
	}

	/**
	 * @param response
	 * @param projectId
	 * @return
	 */
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	@RequestMapping(value = "/getAnalysisReport", method = RequestMethod.POST)
	public @ResponseBody String getAnalysisReport(HttpServletResponse response,
			@RequestParam(value = "projectKey", required = false) String projectKey) {

		HttpResponse analysisReportResponse = sonarAnalysisService.getAnalysisReport(projectKey);
		return sonarAnalysisService.processResponse(analysisReportResponse);
	}
}
