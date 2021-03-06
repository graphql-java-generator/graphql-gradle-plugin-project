package com.graphql_java_generator.gradleplugin;

import java.io.Serializable;

import org.gradle.api.Project;

import com.graphql_java_generator.plugin.conf.GraphQLConfiguration;
import com.graphql_java_generator.plugin.conf.PluginMode;

/**
 * Parameters for the GraphQL Gradle plugin.
 * 
 * @author EtienneSF
 *
 */
public class GraphQLExtension extends GenerateServerCodeExtension implements GraphQLConfiguration, Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * <P>
	 * <I>Since 1.7.1 version</I>
	 * </P>
	 * <P>
	 * Generates a XxxxResponse class for each query/mutation/subscription, and (if separateUtilityClasses is true) Xxxx
	 * classes in the util subpackage. This allows to keep compatibility with code Developed with the 1.x versions of
	 * the plugin.
	 * </P>
	 * <P>
	 * The best way to use the plugin is to directly use the Xxxx query/mutation/subscription classes, where Xxxx is the
	 * query/mutation/subscription name defined in the GraphQL schema.
	 * </P>
	 * <P>
	 * <B><I>Default value is true</I></B>
	 * </P>
	 */
	private boolean generateDeprecatedRequestResponse = GraphQLConfiguration.DEFAULT_GENERATE_DEPRECATED_REQUEST_RESPONSE
			.equals("true");

	/**
	 * The generation mode: either <I>client</I> or <I>server</I>. Choose client to generate the code which can query a
	 * graphql server or server to generate a code for the server side.
	 */
	private PluginMode mode = PluginMode.valueOf(GraphQLConfiguration.DEFAULT_MODE);

	public GraphQLExtension(Project project) {
		super(project);
	}

	@Override
	public boolean isGenerateDeprecatedRequestResponse() {
		return generateDeprecatedRequestResponse;
	}

	public void setGenerateDeprecatedRequestResponse(boolean generateDeprecatedRequestResponse) {
		this.generateDeprecatedRequestResponse = generateDeprecatedRequestResponse;
	}

	@Override
	public PluginMode getMode() {
		return mode;
	}

	public void setMode(PluginMode mode) {
		this.mode = mode;
	}

}
