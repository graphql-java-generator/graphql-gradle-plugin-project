/**
 * 
 */
package com.graphql_java_generator.gradleplugin;

import java.io.IOException;

import javax.inject.Inject;

import org.gradle.api.UncheckedIOException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import com.graphql_java_generator.plugin.conf.GraphQLConfiguration;
import com.graphql_java_generator.plugin.conf.PluginMode;
import com.graphql_java_generator.plugin.generate_code.GenerateCodeDocumentParser;
import com.graphql_java_generator.plugin.generate_code.GenerateCodeGenerator;

/**
 * <P>
 * <B>This goal is <U>deprecated</U></B>. The <I>graphql</I> goal generates the java code from one or more GraphQL
 * schemas. It allows to work in Java with graphQL, in a schema first approach.
 * </P>
 * <P>
 * It will be maintained in the future 2.x versions. The <I>generateClientCode</I> and <I>generateServerCode</I> should
 * be used instead.
 * </P>
 * The <I>graphql</I> goal has two main modes:
 * <UL>
 * <LI><B>client mode:</B> it does the same jobs as the <I>generateClientCode</I> goal. It generates a class for each
 * query, mutation and subscription type. These classes contain the methods to call the queries, mutations and
 * subscriptions. That is: to execute a query against the GraphQL server, you just have to call one of this method. It
 * also generates the POJOs from the GraphQL schema. The <B>GraphQL response is stored in these POJOs</B>, for an easy
 * and standard use in Java.</LI>
 * <LI><B>server mode:</B> it does the same jobs as the <I>generateServerCode</I> goal. It generates the whole heart of
 * the GraphQL server. The developer has only to develop request to the data. That is the main method (in a jar project)
 * or the main server (in a war project), and all the Spring wiring, based on graphql-java-spring, itself being build on
 * top of graphql-java. It also generates the POJOs. An option allows to annotate them with the standard JPA
 * annotations, to make it easy to link with a database. This goal generates the interfaces for the DataFetchersDelegate
 * (often named providers) that the server needs to implement</LI>
 * </UL>
 * <P>
 * <B>Note:</B> The attribute have no default values: their default values is read from the
 * {@link GenerateCodeCommonExtension}, whose attributes can be either the default value, or a value set in the build
 * script.
 * </P>
 * 
 * @author EtienneSF
 */
public class GraphQLGenerateCodeTask extends GenerateServerCodeTask implements GraphQLConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(GraphQLGenerateCodeTask.class);

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
	private Boolean generateDeprecatedRequestResponse;

	/**
	 * The generation mode: either <I>client</I> or <I>server</I>. Choose client to generate the code which can query a
	 * graphql server or server to generate a code for the server side.
	 */
	private PluginMode mode;

	@Inject
	public GraphQLGenerateCodeTask() {
		super(GraphQLExtension.class);
	}

	public GraphQLGenerateCodeTask(Class<? extends GraphQLExtension> extensionClazz) {
		super(extensionClazz);
	}

	@Override
	@TaskAction
	public void execute() {
		try {

			logger.debug("Executing " + this.getClass().getName());

			// We'll use Spring IoC
			GraphQLGenerateCodeSpringConfiguration.graphqlGenerateCodeConf = this;
			AbstractApplicationContext ctx = new AnnotationConfigApplicationContext(
					GraphQLGenerateCodeSpringConfiguration.class);

			// Let's log the current configuration (this will do something only when in debug mode)
			GraphQLConfiguration pluginConfiguration = ctx.getBean(GraphQLConfiguration.class);
			pluginConfiguration.logConfiguration();

			GenerateCodeDocumentParser documentParser = ctx.getBean(GenerateCodeDocumentParser.class);
			documentParser.parseGraphQLSchemas();

			GenerateCodeGenerator codeGenerator = ctx.getBean(GenerateCodeGenerator.class);
			codeGenerator.generateCode();

			ctx.close();

			registerGeneratedFolders();

			logger.debug("Finished generation of java classes from graphqls files (5)");

		} catch (IOException e) {
			throw new UncheckedIOException(e.getMessage(), e);
		}
	}

	@Input
	@Override
	public PluginMode getMode() {
		return getValue(mode, getExtension().getMode());
	}

	public final void setMode(PluginMode mode) {
		this.mode = mode;
		// This task as being configured. So we'll mark compileJava and processResources as depending on it
		setInitialized(true);
	}

	@Input
	@Override
	final public boolean isGenerateDeprecatedRequestResponse() {
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
		return getMode().equals(PluginMode.client);
	}

	@Override
	protected GraphQLExtension getExtension() {
		return (GraphQLExtension) super.getExtension();
	}
}
