/**
 * 
 */
package com.graphql_java_generator.gradleplugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.graphql_java_generator.plugin.conf.CustomScalarDefinition;
import com.graphql_java_generator.plugin.conf.GenerateCodeCommonConfiguration;
import com.graphql_java_generator.plugin.conf.PluginMode;
import com.graphql_java_generator.plugin.conf.QueryMutationExecutionProtocol;

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
 * <P>
 * <B>Note:</B> The attribute have no default values: their default values is read from the
 * {@link GenerateCodeCommonExtension}, whose attributes can be either the default value, or a value set in the build
 * script.
 * </P>
 * 
 * @author etienne-sf
 */
public class GenerateCodeCommonTask extends CommonTask implements GenerateCodeCommonConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(GenerateCodeCommonTask.class);

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
	private Boolean copyRuntimeSources;

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
	private List<CustomScalarDefinition> customScalars;

	/** The packageName in which the generated classes will be created */
	private String packageName;
	/**
	 * (since 2.0RC1) The {@link QueryMutationExecutionProtocol} to use for GraphQL queries and mutations (not
	 * subscriptions). The allowed values are: http and webSocket.<br/>
	 * The default value is http.
	 */
	QueryMutationExecutionProtocol queryMutationExecutionProtocol;

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
	private Boolean separateUtilityClasses;

	/** The encoding for the generated source files */
	private String sourceEncoding;

	/**
	 * Retrieves the suffix that will be applied to the name of the Spring Beans that are generated for this schema.
	 * It's mandatory if you' using a Spring app and have more than one GraphQL schemas. The default value is an empty
	 * String.
	 */
	private String springBeanSuffix;

	/** The folder where the generated resources will be generated */
	protected String targetResourceFolder;

	/** The folder where the source code for the generated classes will be generated */
	protected String targetSourceFolder;

	/**
	 * (since 2.0RC) If false, it uses jakarta EE8 imports (that begins by javax.). If true, it uses jakarta EE8 imports
	 * (that begins by jakarta.).
	 */
	protected boolean useJakartaEE9;

	@Inject
	public GenerateCodeCommonTask(Class<? extends GenerateCodeCommonExtension> extensionClazz) {
		super(extensionClazz);
	}

	@Input
	@Override
	final public boolean isCopyRuntimeSources() {
		return getValue(copyRuntimeSources, getExtension().isCopyRuntimeSources());
	}

	@Internal
	@Override
	public boolean isGenerateUtilityClasses() {
		return true;
	}

	final public void setCopyRuntimeSources(boolean copyRuntimeSources) {
		this.copyRuntimeSources = copyRuntimeSources;
		// This task as being configured. So we'll mark compileJava and processResources as depending on it
		setInitialized(true);
	}

	@Input
	@Override
	final public List<CustomScalarDefinition> getCustomScalars() {
		return getValue(customScalars, getExtension().getCustomScalars());
	}

	final public void setCustomScalars(CustomScalarDefinition[] customScalars) {
		this.customScalars = Arrays.asList(customScalars);
		// This task as being configured. So we'll mark compileJava and processResources as depending on it
		setInitialized(true);
	}

	@Input
	@Optional
	@Override
	public PluginMode getMode() {
		// This method must be overridden by the real task.
		return null;
	}

	@Input
	@Override
	final public String getPackageName() {
		return getValue(packageName, getExtension().getPackageName());
	}

	final public void setPackageName(String packageName) {
		this.packageName = packageName;
		// This task as being configured. So we'll mark compileJava and processResources as depending on it
		setInitialized(true);
	}

	@Input
	@Override
	final public QueryMutationExecutionProtocol getQueryMutationExecutionProtocol() {
		return getValue(queryMutationExecutionProtocol, getExtension().getQueryMutationExecutionProtocol());
	}

	final public void setQueryMutationExecutionProtocol(QueryMutationExecutionProtocol queryMutationExecutionProtocol) {
		this.queryMutationExecutionProtocol = queryMutationExecutionProtocol;
		// This task as being configured. So we'll mark compileJava and processResources as depending on it
		setInitialized(true);
	}

	@Input
	@Override
	public boolean isSeparateUtilityClasses() {
		return getValue(separateUtilityClasses, getExtension().isSeparateUtilityClasses());
	}

	final public void setSeparateUtilityClasses(boolean separateUtilityClasses) {
		this.separateUtilityClasses = separateUtilityClasses;
		// This task as being configured. So we'll mark compileJava and processResources as depending on it
		setInitialized(true);
	}

	@Input
	@Override
	final public String getSourceEncoding() {
		return getValue(sourceEncoding, getExtension().getSourceEncoding());
	}

	final public void setSourceEncoding(String sourceEncoding) {
		this.sourceEncoding = sourceEncoding;
		// This task as being configured. So we'll mark compileJava and processResources as depending on it
		setInitialized(true);
	}

	@Override
	@Internal
	public String getSpringAutoConfigurationPackage() {
		return GenerateCodeCommonConfiguration.getSpringAutoConfigurationPackage(isSeparateUtilityClasses(),
				getPackageName());
	}

	@Input
	@Override
	final public String getSpringBeanSuffix() {
		return getValue(springBeanSuffix, getExtension().getSpringBeanSuffix());
	}

	public final void setSpringBeanSuffix(String springBeanSuffix) {
		this.springBeanSuffix = springBeanSuffix;
		// This task as being configured. So we'll mark compileJava and processResources as depending on it
		setInitialized(true);
	}

	@OutputDirectory
	@Override
	final public File getTargetClassFolder() {
		return getProject().file("build/classes/java/main");
	}

	@OutputDirectory
	@Override
	final public File getTargetResourceFolder() {
		File file = getFileValue(targetResourceFolder, getExtension().getTargetResourceFolder());
		file.mkdirs();
		return file;
	}

	final public void setTargetResourceFolder(String targetResourceFolder) {
		this.targetResourceFolder = targetResourceFolder;
		// This task as being configured. So we'll mark compileJava and processResources as depending on it
		setInitialized(true);
	}

	@OutputDirectory
	@Override
	final public File getTargetSourceFolder() {
		File file = getFileValue(targetSourceFolder, getExtension().getTargetSourceFolder());
		file.mkdirs();
		return file;
	}

	final public void setTargetSourceFolder(String targetSourceFolder) {
		this.targetSourceFolder = targetSourceFolder;
		// This task as being configured. So we'll mark compileJava and processResources as depending on it
		setInitialized(true);
	}

	@Input
	@Override
	public boolean isUseJakartaEE9() {
		return getValue(useJakartaEE9, getExtension().isUseJakartaEE9());
	}

	final public void setUseJakartaEE9(boolean useJakartaEE9) {
		this.useJakartaEE9 = useJakartaEE9;
		// This task as being configured. So we'll mark compileJava and processResources as depending on it
		setInitialized(true);
	}

	@Override
	protected GenerateCodeCommonExtension getExtension() {
		return (GenerateCodeCommonExtension) super.getExtension();
	}

	@Internal
	@Override
	public boolean isGenerateJacksonAnnotations() {
		return true;
	}

	@Override
	public void registerGeneratedFolders() {
		// Let's add the folders where the sources and resources have been generated to the project
		SourceSet main = ((SourceSetContainer) getProject().getProperties().get("sourceSets"))
				.getByName(SourceSet.MAIN_SOURCE_SET_NAME);
		main.getJava().srcDir(getTargetSourceFolder());

		logger.info("Adding '" + getTargetResourceFolder() + "' folder to the resources folders list for task '"
				+ getName() + "'");

		main.getResources().srcDir(getTargetResourceFolder());

		if (logger.isInfoEnabled()) {
			List<String> paths = new ArrayList<>();
			for (File f : main.getResources().getSrcDirs()) {
				paths.add(f.getAbsolutePath());
			}
			logger.info("Resources folders are: [" + String.join(",", paths) + "]");
		}
	}

}
