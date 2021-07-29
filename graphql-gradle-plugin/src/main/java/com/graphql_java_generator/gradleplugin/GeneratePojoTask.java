/**
 * 
 */
package com.graphql_java_generator.gradleplugin;

import java.io.IOException;

import org.gradle.api.UncheckedIOException;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import com.graphql_java_generator.plugin.conf.GeneratePojoConfiguration;
import com.graphql_java_generator.plugin.conf.GraphQLConfiguration;
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
 * the <I>generateJPAAnnotation</I> plugin parameter set to true. In this mode, as with the <I>generateServerCode</I>,
 * you need to either add the <I>graphql-java-server-dependencies</I> dependencies, or set the <I>copyRuntimeSources</I>
 * plugin parameter to false and add the <I>graphql-java-runtime</I>.</LI>
 * <LI><B>client</B>: The client mode is the default one. This mode generates the same POJO as in server mode, with the
 * addition of the <A HREF="https://github.com/FasterXML/jackson">Jackson</A> annotations. These annotations allows to
 * serialize and unserialize the GraphQL POJO to and from JSON. And the <I>CustomJacksonDeserializers</I> utility class
 * is generated, that allows to deserialize custom scalars and arrays. In this mode, as with the
 * <I>generateServerCode</I>, you need to either add the <I>graphql-java-client-dependencies</I> dependencies, or set
 * the <I>copyRuntimeSources</I> plugin parameter to false and add the <I>graphql-java-runtime</I>.</LI>
 * </UL>
 * <P>
 * <B>Note:</B> The attribute have no default values: their default values is read from the
 * {@link GenerateCodeCommonExtension}, whose attributes can be either the default value, or a value set in the build
 * script.
 * </P>
 * 
 * @author EtienneSF
 */
public class GeneratePojoTask extends GraphQLGenerateCodeTask implements GeneratePojoConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(GeneratePojoTask.class);

	public GeneratePojoTask() {
		super(GeneratePojoExtension.class);
	}

	@Override
	@TaskAction
	public void execute() {
		try {

			logger.debug("Executing " + this.getClass().getName());

			// We'll use Spring IoC
			GeneratePojoSpringConfiguration.graphqlPojoConf = this;
			AbstractApplicationContext ctx = new AnnotationConfigApplicationContext(
					GraphQLGenerateCodeSpringConfiguration.class);

			// Let's log the current configuration (this will do something only when in debug mode)
			GraphQLConfiguration pluginConfiguration = ctx.getBean(GraphQLConfiguration.class);
			pluginConfiguration.logConfiguration();

			GenerateCodeDocumentParser documentParser = ctx.getBean(GenerateCodeDocumentParser.class);
			documentParser.parseDocuments();

			GenerateCodeGenerator codeGenerator = ctx.getBean(GenerateCodeGenerator.class);
			codeGenerator.generateCode();

			ctx.close();

			registerGeneratedFolders();

			logger.debug("Finished generation of java classes from graphqls files (5)");

		} catch (IOException e) {
			throw new UncheckedIOException(e.getMessage(), e);
		}
	}

	@Override
	protected GeneratePojoExtension getExtension() {
		return (GeneratePojoExtension) super.getExtension();
	}
}
