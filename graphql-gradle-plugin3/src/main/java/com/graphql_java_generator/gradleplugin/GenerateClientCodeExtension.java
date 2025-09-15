package com.graphql_java_generator.gradleplugin;

import org.gradle.api.file.ProjectLayout;

import com.graphql_java_generator.plugin.conf.GenerateClientCodeConfiguration;
import com.graphql_java_generator.plugin.conf.GraphQLConfiguration;
import com.graphql_java_generator.plugin.conf.PluginMode;

public class GenerateClientCodeExtension extends GenerateCodeCommonExtension
		implements GenerateClientCodeConfiguration {

	private boolean generateDeprecatedRequestResponse = GraphQLConfiguration.DEFAULT_GENERATE_DEPRECATED_REQUEST_RESPONSE
			.equals("true");

	public GenerateClientCodeExtension(ProjectLayout projectLayout) {
		super(projectLayout);
	}

	@Override
	public boolean isGenerateDeprecatedRequestResponse() {
		return generateDeprecatedRequestResponse;
	}

	public void setGenerateDeprecatedRequestResponse(boolean generateDeprecatedRequestResponse) {
		this.generateDeprecatedRequestResponse = generateDeprecatedRequestResponse;
		// This task is now configured. So we'll mark compileJava and processResources as depending on it
		setInitialized(true);
	}

	/** The mode is forced to {@link PluginMode#client} */
	@Override
	public PluginMode getMode() {
		return PluginMode.client;
	}
}
