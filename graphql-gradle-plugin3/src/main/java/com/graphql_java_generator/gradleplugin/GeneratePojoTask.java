/**
 * 
 */
package com.graphql_java_generator.gradleplugin;

import java.io.IOException;
import java.io.UncheckedIOException;

import javax.inject.Inject;

import org.gradle.api.Project;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import com.graphql_java_generator.plugin.conf.GeneratePojoConfiguration;
import com.graphql_java_generator.plugin.conf.GraphQLConfiguration;
import com.graphql_java_generator.plugin.conf.PluginMode;
import com.graphql_java_generator.plugin.generate_code.GenerateCodeDocumentParser;
import com.graphql_java_generator.plugin.generate_code.GenerateCodeGenerator;

/**
 * <P>
 * The <I>generatePojo</I> goal generates all the java objects that match the provided GraphQL schema. It allows to work
 * in Java with graphQL, in a schema first approach.
 * </P>
 * This goal generates:
 * <UL>
 * <LI>One java interface for each GraphQL `union` and `interface`</LI>
 * <LI>One java class for each GraphQL `type` and `input` type, including the query, mutation and subscription (if any).
 * If the GraphQL type implements an interface, then its java class implements this same interface</LI>
 * <LI>One java enum for each GraphQL enum</LI>
 * </UL>
 * 
 * <P>
 * Every class, interface and their attributes are marked with the annotation from the <A HREF=
 * "https://graphql-maven-plugin-project.graphql-java-generator.com/graphql-java-runtime/apidocs/com/graphql_java_generator/annotation/package-summary.html">GraphQL
 * annotation</A> package. This allows to retrieve the GraphQL information for every class, interface and attribute, at
 * runtime.
 * 
 * <P>
 * It can run in two modes (see the <A HREF=
 * "https://graphql-maven-plugin-project.graphql-java-generator.com/graphql-maven-plugin/generatePojo-mojo.html#mode">mode
 * plugin parameter</A> for more information):
 * </P>
 * <UL>
 * <LI><B>server</B>: In the server mode, only the GraphQL annotation are added. You can add the JPA annotation, with
 * the <I>generateJPAAnnotation</I> plugin parameter set to true.</LI>
 * <LI><B>client</B>: The client mode is the default one. This mode generates the same POJO as in server mode, with the
 * addition of the <A HREF="https://github.com/FasterXML/jackson">Jackson</A> annotations. These annotations allows to
 * serialize and unserialize the GraphQL POJO to and from JSON. And the <I>CustomJacksonDeserializers</I> utility class
 * is generated, that allows to deserialize custom scalars and arrays.</LI>
 * </UL>
 * </P>
 * <P>
 * If <B>false</B> (default value since 2.0, recommended), you must add the runtime dependency, for the client or
 * server, depending on the <code>mode</code> parameter you choosed. So the needed dependency would one of these two:
 * </P>
 * 
 * <PRE>
		<dependency>
			<groupId>com.graphql-java-generator</groupId>
			<artifactId>graphql-java-client-runtime</artifactId>
			<version>${graphql-plugin.version}</version>
		</dependency>
 * </PRE>
 * 
 * <PRE>
		<dependency>
			<groupId>com.graphql-java-generator</groupId>
			<artifactId>graphql-java-server-runtime</artifactId>
			<version>${graphql-plugin.version}</version>
		</dependency>
 * </PRE>
 * <P>
 * If <B>true</B>, you must add the runtime dependency, for the client or server, depending on the <code>mode</code>
 * parameter you choosed. So the needed dependency would one of these two:
 * </P>
 * 
 * <PRE>
		<dependency>
			<groupId>com.graphql-java-generator</groupId>
			<artifactId>graphql-java-client-dependencies</artifactId>
			<type>pom</type>
			<version>${graphql-plugin.version}</version>
		</dependency>
 * </PRE>
 * 
 * <PRE>
		<dependency>
			<groupId>com.graphql-java-generator</groupId>
			<artifactId>graphql-java-server-dependencies</artifactId>
			<type>pom</type>
			<version>${graphql-plugin.version}</version>
		</dependency>
 * </PRE>
 * 
 * @author EtienneSF
 */
public class GeneratePojoTask extends GraphQLGenerateCodeTask implements GeneratePojoConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(GeneratePojoTask.class);

	/**
	 * <P>
	 * The <A HREF="https://github.com/FasterXML/jackson">Jackson</A> annotations are necessary to properly deserialize
	 * the json, that is incoming from the GraphQL Server. Setting this property to false allows to not generate them.
	 * </P>
	 * <P>
	 * If this property is set to true, the Jackson annotations are added in the generated GraphQL objects. The
	 * <A HREF="https://github.com/FasterXML/jackson">Jackson</A> dependencies must then be added to the target project,
	 * so that the project compiles.
	 * </P>
	 * <P>
	 * The default value is:
	 * </P>
	 * <UL>
	 * <LI><I>true</I> when in <I>client</I> mode.</LI>
	 * <LI><I>false</I> when in <I>server</I> mode.</LI>
	 * </UL>
	 */
	Boolean generateJacksonAnnotations = null;

	/**
	 * @param projectLayout
	 *            This Gradle service is automatically injected by gradle. It allows to retrieve the project directory,
	 *            as accessing the Gradle {@link Project} is forbidden from a task.
	 */
	@Inject
	public GeneratePojoTask(ProjectLayout projectLayout) {
		super(new GeneratePojoExtension(projectLayout), projectLayout);
	}

	public GeneratePojoTask(GeneratePojoExtension extension, ProjectLayout projectLayout) {
		super(extension, projectLayout);
	}

	@Override
	@TaskAction
	public void execute() {
		try {

			logger.debug("Executing " + this.getClass().getName());

			// We'll use Spring IoC
			GeneratePojoSpringConfiguration.graphqlPojoConf = this;
			AbstractApplicationContext ctx = new AnnotationConfigApplicationContext(
					GeneratePojoSpringConfiguration.class);

			// Let's log the current configuration (this will do something only when in debug mode)
			GraphQLConfiguration pluginConfiguration = ctx.getBean(GraphQLConfiguration.class);
			pluginConfiguration.logConfiguration();

			GenerateCodeDocumentParser documentParser = ctx.getBean(GenerateCodeDocumentParser.class);
			documentParser.parseGraphQLSchemas();

			GenerateCodeGenerator codeGenerator = ctx.getBean(GenerateCodeGenerator.class);
			codeGenerator.generateCode();

			ctx.close();

			logger.debug("Finished generation of java classes from graphqls files (5)");

		} catch (IOException e) {
			throw new UncheckedIOException(e.getMessage(), e);
		}
	}

	@Override
	protected GeneratePojoExtension getExtension() {
		return (GeneratePojoExtension) super.getExtension();
	}

	@Override
	@Input
	public boolean isGenerateJacksonAnnotations() {
		if (generateJacksonAnnotations == null && getExtension().isGenerateJacksonAnnotations_Raw() == null) {
			// Both stored values are null. We select the default value according to the plugin mode
			return getMode().equals(PluginMode.client);
		} else {
			return getValue(generateJacksonAnnotations, getExtension().isGenerateJacksonAnnotations_Raw());
		}
	}

	public void setGenerateJacksonAnnotations(boolean generateJacksonAnnotations) {
		this.generateJacksonAnnotations = generateJacksonAnnotations;
		// This task as being configured. So we'll mark compileJava and processResources as depending on it
		setInitialized(true);
	}

	@Override
	public boolean isGenerateUtilityClasses() {
		return false;
	}

	/**
	 * There is no utility classes for this goal.
	 * 
	 * @return The {@link GeneratePojoConfiguration} implementation of this method always returns false
	 */
	@Override
	public boolean isSeparateUtilityClasses() {
		return true;
	}
}
