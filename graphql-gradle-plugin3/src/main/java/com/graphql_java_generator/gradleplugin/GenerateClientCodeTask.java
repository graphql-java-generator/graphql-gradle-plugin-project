package com.graphql_java_generator.gradleplugin;

import java.io.IOException;

import javax.inject.Inject;

import org.gradle.api.Project;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import com.graphql_java_generator.plugin.conf.GenerateClientCodeConfiguration;
import com.graphql_java_generator.plugin.conf.PluginMode;
import com.graphql_java_generator.plugin.generate_code.GenerateCodeDocumentParser;
import com.graphql_java_generator.plugin.generate_code.GenerateCodeGenerator;

/**
 * <P>
 * The <I>generateClientCode</I> Maven goal (and Gradle task) generates the java code from one or more GraphQL schemas.
 * It allows to work in Java with graphQL, in a schema first approach.
 * </P>
 * <P>
 * It generates a class for each query, mutation and subscription type. These classes contain the methods to call the
 * queries, mutations and subscriptions. That is: to execute a query against the GraphQL server, you just have to call
 * one of these methods. It also generates the POJOs from the GraphQL schema. The <B>GraphQL response is stored in these
 * POJOs</B>, for an easy and standard use in Java.
 * </P>
 * <P>
 * You'll find more info in the tutorials: take a look at the
 * <A HREF="https://github.com/graphql-java-generator/GraphQL-Forum-Maven-Tutorial-client">Maven client tutorial</A> or
 * the <A HREF="https://github.com/graphql-java-generator/GraphQL-Forum-Gradle-Tutorial-client">Gradle client
 * tutorial</A>
 * </P>
 * <P>
 * <B>Note:</B> The attribute have no default values: their default values is read from the
 * {@link GenerateCodeCommonExtension}, whose attributes can be either the default value, or a value set in the build
 * script.
 * </P>
 */
public class GenerateClientCodeTask extends GenerateCodeCommonTask implements GenerateClientCodeConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(GenerateClientCodeTask.class);

	/**
	 * <P>
	 * <I>(since 1.7.1 version) Default value is _true_ for 1.x version, and _false_ for version 2.0 and after.</I>
	 * </P>
	 * <P>
	 * If this parameter is set to true, the plugin generates a XxxxResponse class for each query/mutation/subscription,
	 * and (if separateUtilityClasses is true) Xxxx classes in the util subpackage. This allows to keep compatibility
	 * with code Developed with the 1.x versions of the plugin.
	 * </P>
	 * <P>
	 * The recommended way to use the plugin is to directly use the Xxxx query/mutation/subscription executor classes,
	 * where Xxxx is the query/mutation/subscription name defined in the GraphQL schema. To do this, set this parameter
	 * to _false_, and use the plugin as described in the
	 * <a href="https://github.com/graphql-java-generator/graphql-maven-plugin-project/wiki/client_spring">wiki client
	 * page</a>.
	 * </P>
	 */
	private Boolean generateDeprecatedRequestResponse;

	/**
	 * @param projectLayout
	 *            This Gradle service is automatically injected by gradle. It allows to retrieve the project directory,
	 *            as accessing the Gradle {@link Project} is forbidden from a task.
	 */
	@Inject
	public GenerateClientCodeTask(ProjectLayout projectLayout) {
		super(new GenerateClientCodeExtension(projectLayout), projectLayout);
	}

	@TaskAction
	public void execute() throws IOException {

		logger.info("Executing " + this.getClass().getName());

		// We'll use Spring IoC
		GenerateClientCodeSpringConfiguration.generateClientCodeConf = this;
		AbstractApplicationContext ctx = new AnnotationConfigApplicationContext(
				GenerateClientCodeSpringConfiguration.class);

		// Let's log the current configuration (this will do something only when in debug mode)
		GenerateClientCodeConfiguration pluginConfiguration = ctx.getBean(GenerateClientCodeConfiguration.class);
		pluginConfiguration.logConfiguration();

		// The commented code, below, is use to check the access to a velocity custom template that would be added to
		// buildscript classpath. It works fine with Maven, but doesn't seem to be possible with Gradle. This code
		// remains, in case a hint is found.
		// String path = "classpath:templates/resttemplate/client_query_mutation_type.vm.java";
		// Resource res = ctx.getResource(path);
		// System.out.println(res.getFilename());
		// res.getInputStream().close(); // Just to check that the resource exists
		// System.out.println(res.getFilename() + " actually exists");

		GenerateCodeDocumentParser documentParser = ctx.getBean(GenerateCodeDocumentParser.class);
		documentParser.parseGraphQLSchemas();

		GenerateCodeGenerator codeGenerator = ctx.getBean(GenerateCodeGenerator.class);
		codeGenerator.generateCode();

		ctx.close();

		logger.debug("Finished generation of java classes from graphqls files (5)");
	}

	@Input
	@Override
	public boolean isGenerateDeprecatedRequestResponse() {
		return getValue(generateDeprecatedRequestResponse, getExtension().isGenerateDeprecatedRequestResponse());
	}

	public final void setGenerateDeprecatedRequestResponse(boolean generateDeprecatedRequestResponse) {
		this.generateDeprecatedRequestResponse = generateDeprecatedRequestResponse;
		// This task as being configured. So we'll mark compileJava and processResources as depending on it
		setInitialized(true);
	}

	@Internal
	@Override
	public boolean isGenerateJacksonAnnotations() {
		return true;
	}

	@Override
	protected GenerateClientCodeExtension getExtension() {
		return (GenerateClientCodeExtension) super.getExtension();
	}

	@Input
	@Override
	public PluginMode getMode() {
		return PluginMode.client;
	}
}
