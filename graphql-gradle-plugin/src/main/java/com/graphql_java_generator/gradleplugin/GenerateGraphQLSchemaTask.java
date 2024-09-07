/**
 * 
 */
package com.graphql_java_generator.gradleplugin;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import org.gradle.api.file.ProjectLayout;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import com.graphql_java_generator.plugin.conf.GenerateGraphQLSchemaConfiguration;
import com.graphql_java_generator.plugin.generate_schema.GenerateGraphQLSchema;
import com.graphql_java_generator.plugin.generate_schema.GenerateGraphQLSchemaDocumentParser;

/**
 * <P>
 * The <I>generateGraphQLSchema</I> goal generates GraphQL schema, based on the source GraphQL schemas, and possibly
 * containing additional stuff, like the Relay connection objects.
 * </P>
 * It can be used to:
 * <UL>
 * <LI>Generate several GraphQL schema files into one file, for instance with additional schema files that would use the
 * <I>extend</I> GraphQL keyword</LI>
 * <LI>Reformat the schema file</LI>
 * <LI>Generate the GraphQL schema with the Relay Connection stuff (Node interface, XxxEdge and XxxConnection types),
 * thanks to the <I>addRelayConnections</I> plugin parameter.
 * </UL>
 * <P>
 * This goal is, by default, attached to the Initialize maven phase, to be sure that the GraphQL schema are generated
 * before the code generation would need it, if relevant.
 * </P>
 * <P>
 * <B>Note:</B> The attribute have no default values: their default values is read from the
 * {@link GenerateCodeCommonExtension}, whose attributes can be either the default value, or a value set in the build
 * script.
 * </P>
 * 
 * @author EtienneSF
 */
public class GenerateGraphQLSchemaTask extends CommonTask implements GenerateGraphQLSchemaConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(GenerateGraphQLSchemaTask.class);

	/** The encoding for the generated resource files */
	String resourceEncoding;

	/** The folder where the generated GraphQL schema will be stored */
	private File targetFolder;

	/**
	 * The name of the target filename, in which the schema is generated. This file is stored in the folder, defined in
	 * the <I>targetFolder</I> plugin parameter.
	 */
	private String targetSchemaFileName;

	@Inject
	public GenerateGraphQLSchemaTask(ProjectLayout projectLayout) {
		super(new GenerateGraphQLSchemaExtension(projectLayout), projectLayout);
	}

	public GenerateGraphQLSchemaTask(GenerateGraphQLSchemaExtension extension, ProjectLayout projectLayout) {
		super(extension, projectLayout);
	}

	@TaskAction
	public void execute() throws IOException {

		// We'll use Spring IoC
		GenerateGraphQLSchemaSpringConfiguration.generateGraphQLSchemaConf = this;
		AbstractApplicationContext ctx = new AnnotationConfigApplicationContext(
				GenerateGraphQLSchemaSpringConfiguration.class);

		// Let's log the current configuration (this will do something only when in debug mode)
		GenerateGraphQLSchemaConfiguration pluginConfiguration = ctx.getBean(GenerateGraphQLSchemaConfiguration.class);
		pluginConfiguration.logConfiguration();

		GenerateGraphQLSchemaDocumentParser documentParser = ctx.getBean(GenerateGraphQLSchemaDocumentParser.class);
		documentParser.parseGraphQLSchemas();

		GenerateGraphQLSchema merge = ctx.getBean(GenerateGraphQLSchema.class);
		merge.generateGraphQLSchema();

		ctx.close();

		logger.debug("Finished generation of the merged schema");
	}

	@Override
	@Input
	public String getResourceEncoding() {
		return getValue(resourceEncoding, getExtension().getResourceEncoding());
	}

	public String setResourceEncoding(String resourceEncoding) {
		// This task as being configured. So we'll mark compileJava and processResources as depending on it
		setInitialized(true);

		return this.resourceEncoding = resourceEncoding;
	}

	@Override
	@InputDirectory
	public File getTargetFolder() {
		File file = getValue(targetFolder, getExtension().getTargetFolder());
		file.mkdirs();
		return file;
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

		// This task as being configured. So we'll mark compileJava and processResources as depending on it
		setInitialized(true);
	}

	@Override
	@Input
	public String getTargetSchemaFileName() {
		return getValue(targetSchemaFileName, getExtension().getTargetSchemaFileName());
	}

	public void setTargetSchemaFileName(String targetSchemaFileName) {
		this.targetSchemaFileName = targetSchemaFileName;
		// This task as being configured. So we'll mark compileJava and processResources as depending on it
		setInitialized(true);
	}

	@Override
	protected GenerateGraphQLSchemaExtension getExtension() {
		return (GenerateGraphQLSchemaExtension) super.getExtension();
	}

	@Internal
	@Override
	public boolean isGenerateJacksonAnnotations() {
		return true;
	}

}
