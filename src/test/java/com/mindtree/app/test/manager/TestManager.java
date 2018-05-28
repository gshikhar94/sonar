package com.mindtree.app.test.manager;

import java.util.ArrayList;

import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;

import com.aig.dcp.cmn.log.Logger;
import com.mindtree.app.tester.SonarMultipleRequestsAnalysisTest;

public class TestManager extends Suite {
	private static final Logger logger = Logger.getLogger(TestManager.class);

	protected TestManager(Class<?> klass, Class<?>[] suiteClasses) throws InitializationError {
		super(klass, suiteClasses);
	}

	public TestManager(Class<?> klass) throws InitializationError {
		super(klass, getClassesToBeTested(klass));
	}

	private static Class<?>[] getClassesToBeTested(Class<?> klass) {
		Class<?>[] classesToJunit = null;
		ArrayList<Class<?>> classesTobeTested = new ArrayList<Class<?>>();

		// Add the classes needs to be tested here
		classesTobeTested.add(SonarMultipleRequestsAnalysisTest.class);

		classesToJunit = new Class<?>[classesTobeTested.size()]; // Initialize
																	// the array
		classesToJunit = classesTobeTested.toArray(classesToJunit);

		if (logger.isInfo()) {
			logger.info("Classes to be tested :" + classesTobeTested);
		}

		return classesToJunit;

	}
}
