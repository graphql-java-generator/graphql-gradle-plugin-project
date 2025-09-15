package com.graphql_java_generator.gradleplugin;

import java.io.File;
import java.io.Serializable;

import org.gradle.api.file.ProjectLayout;

import com.graphql_java_generator.plugin.conf.GenerateGraphQLSchemaConfiguration;

/**
 * Parameters for the GraphQL Gradle plugin.
 * 
 * @author EtienneSF
 *
 */
public class GenerateGraphQLSchemaExtension extends CommonExtension
		implements GenerateGraphQLSchemaConfiguration, Serializable {

	private static final long serialVersionUID = 1L;

	/** The encoding for the generated resource files */
	String resourceEncoding = GenerateGraphQLSchemaConfiguration.DEFAULT_RESOURCE_ENCODING;

	/** The folder where the generated GraphQL schema will be stored */
	private File targetFolder = null;

	/** The folder where the generated resources will be generated */
	protected String targetResourceFolder = "./build/generated/resources/graphqlGradlePlugin";

	/**
	 * The name of the target filename, in which the schema is generated. This file is stored in the folder, defined in
	 * the <I>targetFolder</I> plugin parameter.
	 */
	private String targetSchemaFileName = GenerateGraphQLSchemaConfiguration.DEFAULT_TARGET_SCHEMA_FILE_NAME;

	public GenerateGraphQLSchemaExtension(ProjectLayout projectLayout) {
		super(projectLayout);
	}

	@Override
	public String getResourceEncoding() {
		return resourceEncoding;
	}

	public void setResourceEncoding(String resourceEncoding) {
		this.resourceEncoding = resourceEncoding;
		// This task is now configured. So we'll mark compileJava and processResources as depending on it
		setInitialized(true);
	}

	@Override
	public File getTargetFolder() {
		return (targetFolder != null) ? targetFolder
				: new File(getProjectBuildDir(), GenerateGraphQLSchemaConfiguration.DEFAULT_TARGET_FOLDER);
	}

	/**
	 * 
	 * @param targetFolder
	 *            A folder, relative to the project dir (not the the build dir)
	 */
	public void setTargetFolder(String targetFolder) {
		this.targetFolder = new File(getProjectDir(), targetFolder);

		// Let's create the folder now, so that it exists when if any other task needs it, during configuration time
		this.targetFolder.mkdirs();

		// This task is now configured. So we'll mark compileJava and processResources as depending on it
		setInitialized(true);
	}

	@Override
	public String getTargetSchemaFileName() {
		return targetSchemaFileName;
	}

	public void setTargetSchemaFileName(String targetSchemaFileName) {
		this.targetSchemaFileName = targetSchemaFileName;
		// This task is now configured. So we'll mark compileJava and processResources as depending on it
		setInitialized(true);
	}

}
