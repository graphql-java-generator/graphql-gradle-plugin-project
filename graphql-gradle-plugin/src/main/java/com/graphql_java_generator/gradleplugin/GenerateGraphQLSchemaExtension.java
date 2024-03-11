package com.graphql_java_generator.gradleplugin;

import java.io.File;
import java.io.Serializable;

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
	private String targetFolder = GenerateGraphQLSchemaConfiguration.DEFAULT_TARGET_FOLDER;

	/** The folder where the generated resources will be generated */
	protected String targetResourceFolder = "./build/generated/resources/graphqlGradlePlugin";

	/**
	 * The name of the target filename, in which the schema is generated. This file is stored in the folder, defined in
	 * the <I>targetFolder</I> plugin parameter.
	 */
	private String targetSchemaFileName = GenerateGraphQLSchemaConfiguration.DEFAULT_TARGET_SCHEMA_FILE_NAME;

	public GenerateGraphQLSchemaExtension(File projectDir) {
		super(projectDir);
	}

	@Override
	public String getResourceEncoding() {
		return this.resourceEncoding;
	}

	public void setResourceEncoding(String resourceEncoding) {
		this.resourceEncoding = resourceEncoding;
		// This task as being configured. So we'll mark compileJava and processResources as depending on it
		setInitialized(true);
	}

	@Override
	public File getTargetFolder() {
		return new File(this.projectDir, this.targetFolder);
	}

	public void setTargetFolder(String targetFolder) {
		// Let's create the folder now, so that it exists when if any other task needs it, during configuration time
		new File(this.projectDir, targetFolder).mkdirs();

		this.targetFolder = targetFolder;

		// This task as being configured. So we'll mark compileJava and processResources as depending on it
		setInitialized(true);
	}

	@Override
	public String getTargetSchemaFileName() {
		return this.targetSchemaFileName;
	}

	public void setTargetSchemaFileName(String targetSchemaFileName) {
		this.targetSchemaFileName = targetSchemaFileName;
		// This task as being configured. So we'll mark compileJava and processResources as depending on it
		setInitialized(true);
	}

}
