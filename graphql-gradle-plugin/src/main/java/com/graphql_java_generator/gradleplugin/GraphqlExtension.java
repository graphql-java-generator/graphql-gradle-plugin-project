package com.graphql_java_generator.gradleplugin;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gradle.api.Project;

import com.graphql_java_generator.plugin.CustomScalarDefinition;
import com.graphql_java_generator.plugin.Logger;
import com.graphql_java_generator.plugin.Packaging;
import com.graphql_java_generator.plugin.PluginConfiguration;
import com.graphql_java_generator.plugin.PluginMode;

import graphql.schema.GraphQLScalarType;

/**
 * Parameters for the GraphQL Gradle plugin.
 * 
 * @author EtienneSF
 *
 */
public class GraphqlExtension implements PluginConfiguration, Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Flag to enable copy sources for graphql-java-runtime library to target source code directory. It allows to
	 * control whether the runtime code is embedded in the generated code or not. <BR/>
	 * The default behavior is the old one, that is: the runtime code is embedded. This means that when you upgrade the
	 * plugin version, just build the project and everything is coherent.<BR/>
	 * If you set this parameter to false, the runtime is no more copied with the generated code. it's up to you to
	 * provided the runtime in the pom dependencies. This allows you to create your own runtime, and change the
	 * "standard" behavior. But of course, you'll have to check the compatibility with all the next versions.
	 */
	private boolean copyRuntimeSources = PluginConfiguration.DEFAULT_COPY_RUNTIME_SOURCES.equals("true");

	/**
	 * This parameter contains the list of custom scalars implementations. One such implementation must be provided for
	 * each custom scalar defined in the GraphQL implemented by the project for its GraphQL schema. It's a list, where
	 * the key is the scalar name, as defined in the GraphQL schema, and the value is the full class name of the
	 * implementation of {@link GraphQLScalarType}. <BR/>
	 * This parameter is a list of customScalars. For each one, you must define the name, the javaType and exactly one
	 * of these fields: graphQLScalarTypeClass, graphQLScalarTypeStaticField or graphQLScalarTypeGetter.<BR/>
	 * Here is the detail:
	 * <UL>
	 * <LI><B>graphQLTypeName: The type name, as defined in the GraphQL schema, for instance <I>Date</I></LI>
	 * <LI><B>javaType: The full class name for the java type that contains the data for this type, once in the Java
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
	 * <UL>
	 * Please have a look at the allGraphQLCases (both client and server) samples for more information. The <A HREF=
	 * "https://github.com/graphql-java-generator/graphql-maven-plugin-project/blob/master/graphql-maven-plugin-samples/graphql-maven-plugin-samples-allGraphQLCases-client/pom.xml">allGraphQLCases
	 * client pom</A> is a good sample.
	 */
	private List<CustomScalarDefinition> customScalars = new ArrayList<>();

	/**
	 * Indicates whether the plugin should generate the JPA annotations, for generated objects, when in server mode.
	 */
	private boolean generateJPAAnnotation = PluginConfiguration.DEFAULT_GENERATE_JPA_ANNOTATION.equals("true");

	private GradleLogger logger;

	/**
	 * The generation mode: either <I>client</I> or <I>server</I>. Choose client to generate the code which can query a
	 * graphql server or server to generate a code for the server side.
	 */
	private PluginMode mode = PluginMode.valueOf(PluginConfiguration.DEFAULT_MODE);

	/** The packageName in which the generated classes will be created */
	private String packageName = PluginConfiguration.DEFAULT_PACKAGE_NAME;

	private final Project project;

	/** The folder where the graphql schema file(s) will be searched. The default schema is the main resource folder. */
	private String schemaFileFolder = PluginConfiguration.DEFAULT_SCHEMA_FILE_FOLDER;

	/**
	 * The pattern to find the graphql schema file(s). The default value is "/*.graphqls" meaning that the maven plugin
	 * will search all graphqls files in the "/src/main/resources" folder (please check also the <I>schemaFileFolder</I>
	 * plugin parameter).
	 */
	private String schemaFilePattern = PluginConfiguration.DEFAULT_SCHEMA_FILE_PATTERN;

	/**
	 * schemaPersonalizationFile is the file name where the GraphQL maven plugin will find personalization that it must
	 * apply before generating the code. This applies to the <B>server</B> mode only. See
	 * <A HREF="https://graphql-maven-plugin-project.graphql-java-generator.com/schema_personalization.html">the doc on
	 * the plugin web site</A> for more details.<BR/>
	 * The standard file would be something like /src/main/graphql/schemaPersonalizationFile.json, which avoids to embed
	 * this compile time file within your maven artifact (as it is not in the /src/main/java nor in the
	 * /src/main/resources folders).
	 */
	private String schemaPersonalizationFile = PluginConfiguration.DEFAULT_SCHEMA_PERSONALIZATION_FILE;

	/**
	 * Indicates whether the utility classes (that is: the classes that are not match an item in the GraphQL schema) are
	 * generated in the same package than the classes that matches the GraphQL schema.<BR/>
	 * That is: internal technical classes, java classes that contain the method to execute the
	 * queries/mutations/subscriptions, Jackson deserializer for custom scalars...<BR/>
	 * The default value is false, to maintain the previous behavior. In this case, all classes are generated in the
	 * <I>packageName</I>, or the default package if this parameter is not defined.<BR/>
	 * If true, the GraphQL classes are generated in the package defined in the <I>packageName</I> plugin parameter. And
	 * all the utility classes are generated in the <I>util</I> subpackage of this package.
	 */
	private boolean separateUtilityClasses = PluginConfiguration.DEFAULT_SEPARATE_UTIL_CLASSES.equals("true");

	/** The encoding for the generated source files */
	private String sourceEncoding = PluginConfiguration.DEFAULT_SOURCE_ENCODING;

	/**
	 * Map of the code templates to be used: this allows to override the default templates, and control exactly what
	 * code is generated by the plugin.<BR/>
	 * You can override any of the Velocity templates of the project. The list of templates is defined in the enum
	 * CodeTemplate, that you can <A HREF=
	 * "https://github.com/graphql-java-generator/graphql-maven-plugin-project/blob/master/graphql-maven-plugin-logic/src/main/java/com/graphql_java_generator/plugin/CodeTemplate.java">check
	 * here</A>.<BR/>
	 * You can find a sample in the <A HREF=
	 * "https://github.com/graphql-java-generator/graphql-maven-plugin-project/blob/master/graphql-maven-plugin-samples/graphql-maven-plugin-samples-CustomTemplates-client/pom.xml">CustomTemplates
	 * client sample</A>.<BR/>
	 * <B>Important notice:</B> Please note that the default templates may change in the future. And some of these
	 * modifications would need to be reported into the custom templates. We'll try to better expose a stable public API
	 * in the future.
	 */
	private Map<String, String> templates = new HashMap<>();

	public GraphqlExtension(Project project) {
		this.project = project;
		this.logger = new GradleLogger(project);
	}

	/** {@inheritDoc} */
	@Override
	public List<CustomScalarDefinition> getCustomScalars() {
		return customScalars;
	}

	/** {@inheritDoc} */
	@Override
	public Logger getLog() {
		return logger;
	}

	/** {@inheritDoc} */
	@Override
	public PluginMode getMode() {
		return mode;
	}

	/** {@inheritDoc} */
	@Override
	public String getPackageName() {
		return packageName;
	}

	/** {@inheritDoc} */
	@Override
	public Packaging getPackaging() {
		// We calculate as late as possible this packaging. So no precaculation on creation, we wait for a call on this
		// getter. At this time, it should be triggered by the gradle plugin execution (and not its configuration)
		return (project.getTasksByName("war", false).size() >= 1) ? Packaging.war : Packaging.jar;
	}

	/** {@inheritDoc} */
	@Override
	public File getSchemaFileFolder() {
		return project.file(schemaFileFolder);
	}

	/** {@inheritDoc} */
	@Override
	public String getSchemaFilePattern() {
		return schemaFilePattern;
	}

	/** {@inheritDoc} */
	@Override
	public File getSchemaPersonalizationFile() {
		return (PluginConfiguration.DEFAULT_SCHEMA_PERSONALIZATION_FILE.equals(schemaPersonalizationFile)) ? null
				: project.file(schemaPersonalizationFile);
	}

	/** {@inheritDoc} */
	@Override
	public String getSourceEncoding() {
		return sourceEncoding;
	}

	/** {@inheritDoc} */
	@Override
	public File getTargetClassFolder() {
		// TODO Understand why project.file("$buildDir/classes") doesn't work
		return project.file("build/classes");
	}

	/** {@inheritDoc} */
	@Override
	public File getTargetSourceFolder() {
		// TODO Understand why project.file("$buildDir/classes") doesn't work
		return project.file("build/generated/" + GraphqlPlugin.GRAPHQL_GENERATE_CODE_TASK_NAME);
	}

	/** {@inheritDoc} */
	@Override
	public Map<String, String> getTemplates() {
		return templates;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isCopyRuntimeSources() {
		return copyRuntimeSources;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isGenerateJPAAnnotation() {
		return generateJPAAnnotation;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isSeparateUtilityClasses() {
		return separateUtilityClasses;
	}

	/** {@inheritDoc} */
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public void setCopyRuntimeSources(boolean copyRuntimeSources) {
		this.copyRuntimeSources = copyRuntimeSources;
	}

	public void setCustomScalars(CustomScalarDefinition[] customScalars) {
		this.customScalars = Arrays.asList(customScalars);
	}

	public void setGenerateJPAAnnotation(boolean generateJPAAnnotation) {
		this.generateJPAAnnotation = generateJPAAnnotation;
	}

	public void setLogger(GradleLogger logger) {
		this.logger = logger;
	}

	public void setMode(PluginMode mode) {
		this.mode = mode;
	}

	public void setSchemaFileFolder(File schemaFileFolder) {
		this.schemaFileFolder = schemaFileFolder.getAbsolutePath();
	}

	public void setSchemaFilePattern(String schemaFilePattern) {
		this.schemaFilePattern = schemaFilePattern;
	}

	public void setSchemaPersonalizationFile(String schemaPersonalizationFile) {
		this.schemaPersonalizationFile = schemaPersonalizationFile;
	}

	public void setSeparateUtilityClasses(boolean separateUtilityClasses) {
		this.separateUtilityClasses = separateUtilityClasses;
	}

	public void setSourceEncoding(String sourceEncoding) {
		this.sourceEncoding = sourceEncoding;
	}

	public void setTemplates(Map<String, String> templates) {
		this.templates = templates;
	}

}
