package com.davidsusanto.api;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;

/**
 * Entry point Gradle discovers.
 * Everything else (plugins, parallelism, default tag filter)
 * lives in {@code junit-platform.properties} so it can be overridden with -D flags.
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("feature")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.davidsusanto.api.steps,com.davidsusanto.api.hooks")
public class RunCucumberTest {
}
