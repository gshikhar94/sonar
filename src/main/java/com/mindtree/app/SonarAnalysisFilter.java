package com.mindtree.app;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
@WebFilter( "/*" )
public class SonarAnalysisFilter implements Filter {
	private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		addAccessControlHeaders(request, response, filterChain);
	}


	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
			throws IOException, ServletException {

		addAccessControlHeaders((HttpServletRequest) request, (HttpServletResponse) response, filterChain);

	}

	private void addAccessControlHeaders(HttpServletRequest request, HttpServletResponse response,
			FilterChain filterChain) throws IOException, ServletException {

		logger.info("Adding all the headers to the response");

		response.addHeader("Access-Control-Allow-Origin", "*");

		if (request.getHeader("Access-Control-Request-Method") != null && "OPTIONS".equals(request.getMethod())) {
			response.addHeader("Access-Control-Allow-Methods", "GET, PUT, DELETE,POST");
			response.addHeader("Access-Control-Allow-Headers", "X-Requested-With,Origin,Content-Type, Accept");

		}

		filterChain.doFilter(request, response);
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {

	}


	@Override
	public void destroy() {
		
	}

}
