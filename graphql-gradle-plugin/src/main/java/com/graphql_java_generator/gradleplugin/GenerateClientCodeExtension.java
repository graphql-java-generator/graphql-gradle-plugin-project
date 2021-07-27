package com.graphql_java_generator.gradleplugin;

import org.gradle.api.Project;

import com.graphql_java_generator.plugin.conf.GenerateClientCodeConfiguration;
import com.graphql_java_generator.plugin.conf.GraphQLConfiguration;
import com.graphql_java_generator.plugin.conf.PluginMode;

public class GenerateClientCodeExtension extends GenerateCodeCommonExtension
		implements GenerateClientCodeConfiguration {

	private boolean generateDeprecatedRequestResponse = GraphQLConfiguration.DEFAULT_GENERATE_DEPRECATED_REQUEST_RESPONSE
			.equals("true");

	public GenerateClientCodeExtension(Project project) {
		super(project);
	}

	@Override
	public boolean isGenerateDeprecatedRequestResponse() {
		return generateDeprecatedRequestResponse;
	}

	public void setGenerateDeprecatedRequestResponse(boolean generateDeprecatedRequestResponse) {
		this.generateDeprecatedRequestResponse = generateDeprecatedRequestResponse;
	}

	/** The mode is forced to {@link PluginMode#client} */
	@Override
	public PluginMode getMode() {
		return PluginMode.client;
	}
}
