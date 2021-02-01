/**
 * 
 */
package com.graphql_java_generator.gradleplugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.gradle.api.Project;

import com.graphql_java_generator.plugin.conf.CustomScalarDefinition;
import com.graphql_java_generator.plugin.conf.GenerateCodeCommonConfiguration;
import com.graphql_java_generator.plugin.conf.GraphQLConfiguration;
import com.graphql_java_generator.plugin.conf.PluginMode;

import graphql.schema.GraphQLScalarType;

/**
 * <P>
 * This class is the super class of all Gradle Extensions that generate code, that is the
 * {@link GenerateClientCodeExtension}, the {@link GenerateServerCodeExtension} and the {@link GraphQLExtension}
 * extensions. It contains all parameters that are common to these tasks. The parameters common to all tasks are
 * inherited from the {@link CommonExtension} class.
 * </P>
 * <P>
 * This avoids to redeclare each common parameter in each task, including its comment. When a comment is updated, only
 * one update is necessary, instead of updating it in each.
 * </P>
 * 
 * @author etienne-sf
 */
public abstract class GenerateCodeCommon extends CommonExtension implements GenerateCodeCommonConfiguration {
	/**
	 * <P>
	 * Flag to enable copy sources for graphql-java-runtime library to target source code directory. It allows to
	 * control whether the runtime code is embedded in the generated code or not.
	 * </P>
	 * <P>
	 * The default behavior is the old one, that is: the runtime code is embedded. This means that when you upgrade the
	 * plugin version, just build the project and everything is coherent.
	 * </P>
	 * <P>
	 * If you set this parameter to false, the runtime is no more copied with the generated code. You then have to add
	 * the runtime dependency in the pom dependencies: it's the com.graphql-java-generator:graphql-java-runtime
	 * dependency, with the exact same version as the plugin version.
	 * </P>
	 * <P>
	 * This also allows you to create your own runtime, and change the "standard" behavior. But of course, you'll have
	 * to check the compatibility with all the next versions.
	 * </P>
	 */
	private boolean copyRuntimeSources = GraphQLConfiguration.DEFAULT_COPY_RUNTIME_SOURCES.equals("true");

	/**
	 * <P>
	 * This parameter contains the list of custom scalars implementations. One such implementation must be provided for
	 * each custom scalar defined in the GraphQL implemented by the project for its GraphQL schema. It's a list, where
	 * the key is the scalar name, as defined in the GraphQL schema, and the value is the full class name of the
	 * implementation of {@link GraphQLScalarType}.
	 * </P>
	 * <P>
	 * This parameter is a list of customScalars. For each one, you must define the name, the javaType and exactly one
	 * of these fields: graphQLScalarTypeClass, graphQLScalarTypeStaticField or graphQLScalarTypeGetter.
	 * </P>
	 * <P>
	 * Here is the detail:
	 * </P>
	 * <UL>
	 * <LI><B>graphQLTypeName</B>: The type name, as defined in the GraphQL schema, for instance <I>Date</I></LI>
	 * <LI><B>javaType</B>: The full class name for the java type that contains the data for this type, once in the Java
	 * code, for instance <I>java.util.Date</I></LI>
	 * <LI><B>graphQLScalarTypeClass</B>: The full class name for the {@link GraphQLScalarType} that will manage this
	 * Custom Scalar. This class must be a subtype of {@link GraphQLScalarType}. Bu the constructor of
	 * {@link GraphQLScalarType} has been deprecated, so you'll find no sample for that in this project</LI>
	 * <LI><B>graphQLScalarTypeStaticField</B>: The full class name followed by the static field name that contains the
	 * {@link GraphQLScalarType} that will manage this Custom Scalar. For instance, the graphql-java package provides
	 * several custom scalars like <I>graphql.Scalars.GraphQLLong</I>. You can also use the
	 * <I>graphql-java-extended-scalars</I> project, that provides other custom scalars like
	 * <I>graphql.scalars.ExtendedScalars.NonNegativeInt</I>.</LI>
	 * <LI><B>graphQLScalarTypeGetter</B>: The full class name followed by the static method name that returns the
	 * {@link GraphQLScalarType} that will manage this Custom Scalar. For instance:
	 * <I>org.mycompany.MyScalars.getGraphQLLong()</I> or
	 * <I>com.graphql_java_generator.customscalars.GraphQLScalarTypeDate</I>. This call may contain parameters, provided
	 * that this a valid java command.</LI>
	 * </UL>
	 * <P>
	 * Please have a look at the allGraphQLCases (both client and server) samples for more information. The <A HREF=
	 * "https://github.com/graphql-java-generator/graphql-maven-plugin-project/blob/master/graphql-maven-plugin-samples/graphql-maven-plugin-samples-allGraphQLCases-client/pom.xml">allGraphQLCases
	 * client pom</A> is a good sample.
	 * </P>
	 */
	private List<CustomScalarDefinition> customScalars = new ArrayList<>();

	/** The packageName in which the generated classes will be created */
	private String packageName = GraphQLConfiguration.DEFAULT_PACKAGE_NAME;

	/**
	 * <P>
	 * Indicates whether the utility classes (that is: the classes that are not match an item in the GraphQL schema) are
	 * generated in the same package than the classes that matches the GraphQL schema.
	 * </P>
	 * <P>
	 * That is: internal technical classes, java classes that contain the method to execute the
	 * queries/mutations/subscriptions, Jackson deserializer for custom scalars...
	 * </P>
	 * <P>
	 * The default value is false, to maintain the previous behavior. In this case, all classes are generated in the
	 * <I>packageName</I>, or the default package if this parameter is not defined.
	 * </P>
	 * <P>
	 * If true, the GraphQL classes are generated in the package defined in the <I>packageName</I> plugin parameter. And
	 * all the utility classes are generated in the <I>util</I> subpackage of this package.
	 * </P>
	 */
	private boolean separateUtilityClasses = GraphQLConfiguration.DEFAULT_SEPARATE_UTIL_CLASSES.equals("true");

	/** The encoding for the generated source files */
	private String sourceEncoding = GraphQLConfiguration.DEFAULT_SOURCE_ENCODING;

	public GenerateCodeCommon(Project project) {
		super(project);
	}

	@Override
	public boolean isCopyRuntimeSources() {
		return copyRuntimeSources;
	}

	public void setCopyRuntimeSources(boolean copyRuntimeSources) {
		this.copyRuntimeSources = copyRuntimeSources;
	}

	@Override
	public List<CustomScalarDefinition> getCustomScalars() {
		return customScalars;
	}

	public void setCustomScalars(CustomScalarDefinition[] customScalars) {
		this.customScalars = Arrays.asList(customScalars);
	}

	@Override
	public abstract PluginMode getMode();

	@Override
	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	@Override
	public boolean isSeparateUtilityClasses() {
		return separateUtilityClasses;
	}

	public void setSeparateUtilityClasses(boolean separateUtilityClasses) {
		this.separateUtilityClasses = separateUtilityClasses;
	}

	@Override
	public String getSourceEncoding() {
		return sourceEncoding;
	}

	public void setSourceEncoding(String sourceEncoding) {
		this.sourceEncoding = sourceEncoding;
	}

	@Override
	public File getTargetClassFolder() {
		// TODO Understand why project.file("$buildDir/classes") doesn't work
		return project.file("build/classes/java/main");
	}

	@Override
	public File getTargetResourceFolder() {
		// TODO Understand why project.file("$buildDir/resources") doesn't work
		return project.file("build/resources/main");
	}

}
