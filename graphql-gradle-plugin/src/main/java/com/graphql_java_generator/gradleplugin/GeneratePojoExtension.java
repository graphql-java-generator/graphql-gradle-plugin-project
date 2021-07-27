package com.graphql_java_generator.gradleplugin;

import java.io.Serializable;

import org.gradle.api.Project;

import com.graphql_java_generator.plugin.conf.GeneratePojoConfiguration;

/**
 * Parameters for the GraphQL Gradle plugin.
 * 
 * @author EtienneSF
 *
 */
public class GeneratePojoExtension extends GraphQLExtension implements GeneratePojoConfiguration, Serializable {

	private static final long serialVersionUID = 1L;

	public GeneratePojoExtension(Project project) {
		super(project);
	}

}
