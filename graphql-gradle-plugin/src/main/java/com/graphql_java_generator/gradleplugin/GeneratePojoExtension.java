package com.graphql_java_generator.gradleplugin;

import java.io.Serializable;

import org.gradle.api.Project;

import com.graphql_java_generator.plugin.conf.GeneratePojoConfiguration;
import com.graphql_java_generator.plugin.conf.GraphQLConfiguration;

/**
 * Parameters for the GraphQL Gradle plugin.
 * 
 * @author EtienneSF
 *
 */
public class GeneratePojoExtension extends GraphQLExtension implements GeneratePojoConfiguration, Serializable {

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

	public GeneratePojoExtension(Project project) {
		super(project);
	}

	@Override
	public boolean isGenerateDeprecatedRequestResponse() {
		return generateDeprecatedRequestResponse;
	}

	@Override
	public void setGenerateDeprecatedRequestResponse(boolean generateDeprecatedRequestResponse) {
		this.generateDeprecatedRequestResponse = generateDeprecatedRequestResponse;
	}

}
