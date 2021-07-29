/**
 * 
 */
package com.graphql_java_generator.gradleplugin;

import java.io.File;
import java.util.Map;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.graphql_java_generator.plugin.conf.CommonConfiguration;
import com.graphql_java_generator.plugin.conf.GenerateGraphQLSchemaConfiguration;

import groovy.lang.Closure;

/**
 * <P>
 * This class contain the parameters that are common to all Gradle Extensions for this plugin, that is the parameters
 * that are common to all the tasks of this plugin.
 * </P>
 * <P>
 * This avoids to redeclare each common parameter in each Extension, including its comment. When a comment is updated,
 * only one update is necessary, instead of updating it in each Extension.
 * </P>
 * <P>
 * <B>Note:</B> The attribute have no default values: their default values is read from the
 * {@link GenerateCodeCommonExtension}, whose attributes can be either the default value, or a value set in the build
 * script.
 * </P>
 * 
 * @author etienne-sf
 */
public class CommonTask extends DefaultTask implements CommonConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(CommonTask.class);

	/**
	 * <P>
	 * True if the plugin is configured to add the relay connection capabilities, as
	 * <A HREF="https://relay.dev/docs/en/graphql-server-specification.html">described here</A> and specified in the
	 * <A HREF="https://relay.dev/graphql/connections.htm">relay connection specification</A>.
	 * </P>
	 * <P>
	 * The plugin reads the GraphQL schema file(s), and enrich them with the interface and types needed to respect the
	 * Relay Connection specification. The entry point for that is the <I>&#064;RelayConnection</I> directive. It is
	 * specific to this plugin. It can be added to any field, that is, typically: queries, mutations, interface's
	 * fields, type's field. It must be declared in the given GraphQL schema file(s) like this:
	 * </P>
	 * 
	 * <PRE>
	 * directive <I>&#064;RelayConnection</I> on FIELD_DEFINITION
	 * </PRE>
	 * <P>
	 * When <I>addRelayConnections</I> is set to true, here is what's done for each field that is marked with the
	 * <I>&#064;RelayConnection</I> directive:
	 * </P>
	 * <UL>
	 * <LI>The field type, whether it's a list or not, is replaced by the relevant XxxConnection type. For instance the
	 * query <I>allHumans(criteria: String): [Human] &#064;RelayConnection</I> is replaced by <I>allHumans(criteria:
	 * String): HumanConnection</I>, and the human's field <I>friends: Character &#064;RelayConnection</I> is replaced
	 * by <I>friends: CharacterConnection</I>. Please note that :
	 * <UL>
	 * <LI>The <I>&#064;RelayConnection</I> directive is removed in the target schema</LI>
	 * <LI>If the <I>&#064;RelayConnection</I> is set on a field of an interface, it should be set also in the same
	 * field, for each type that implements this interface. If not, a warning is generated. The directive is applied on
	 * the interface and its implementations's field, whether or not the directive is actually set in the implementing
	 * classes.</LI>
	 * <LI>If the <I>&#064;RelayConnection</I> is <B>not set</B> on a field of an interface, but is set in the same
	 * field, for one type that implements this interface, then an error is generated. The directive is applied on the
	 * interface and its implementations's field, whether or not the directive is actually set in the implementing
	 * classes.</LI>
	 * <LI>Input type's fields may not have the <I>&#064;RelayConnection</I> directive</LI>
	 * </UL>
	 * </LI>
	 * <LI>For each type marked at least once, with the <I>&#064;RelayConnection</I> directive (the <I>Human</I> type,
	 * and the <I>Character</I> interface, here above), the relevant XxxConnection and XxxEdge type are added to the
	 * in-memory schema.</LI>
	 * <LI>The <I>Node</I> interface is added to each type marked at least once, with the <I>&#064;RelayConnection</I>
	 * directive (the <I>Human</I> type, and the <I>Character</I> interface, here above). Of course, these types must
	 * have a mandatory field <I>id</I> of type <I>ID</I> that is not a list. If not, then an error is thrown.</LI>
	 * </UL>
	 * <P>
	 * As a sum-up, if <I>addRelayConnections</I> is set to true, the plugin will add into the in-memory GraphQL schema:
	 * </P>
	 * <UL>
	 * <LI>Check that the <I>&#064;@RelayConnexion</I> directive definition exist in the GraphQL schema, and is
	 * compliant with the above definition.</LI>
	 * <LI>Add the <I>Node</I> interface in the GraphQL schema (if not already defined). If this interface is already
	 * defined in the given schema, but is not compliant with the relay specification, then an error is thrown.</LI>
	 * <LI>Add the <I>PageInfo</I> type in the GraphQL schema (if not already defined). If this type is already defined
	 * in the given schema, but is not compliant with the relay specification, then an error is thrown.</LI>
	 * <LI>All the Edge and Connection type in the GraphQL schema, for each type that is marked by the
	 * <I>&#064;@RelayConnexion</I> directive.</LI>
	 * </UL>
	 */
	private Boolean addRelayConnections;

	/** The folder where the graphql schema file(s) will be searched. The default schema is the main resource folder. */
	private String schemaFileFolder;

	/**
	 * The pattern to find the graphql schema file(s). The default value is "/*.graphqls" meaning that the maven plugin
	 * will search all graphqls files in the "/src/main/resources" folder (please check also the <I>schemaFileFolder</I>
	 * plugin parameter).
	 */
	private String schemaFilePattern;

	/**
	 * <P>
	 * This parameter is now <B><I>deprecated</I></B>: it's value used in the plugin is always true, that is: if the
	 * generated sources or resources are older than the GraphQL schema file(s), then there is no source or resource
	 * generation. In clear, the source and resource generation is always executed only if the provided input (GraphQL
	 * schema...) has been updated since the last plugin execution.
	 * </P>
	 */
	public Boolean skipGenerationIfSchemaHasNotChanged;

	/**
	 * <P>
	 * Map of the code templates to be used: this allows to override the default templates, and control exactly what
	 * code is generated by the plugin.
	 * </P>
	 * <P>
	 * You can override any of the Velocity templates of the project. The list of templates is defined in the enum
	 * CodeTemplate, that you can <A HREF=
	 * "https://github.com/graphql-java-generator/graphql-maven-plugin-project/blob/master/graphql-maven-plugin-logic/src/main/java/com/graphql_java_generator/plugin/CodeTemplate.java">check
	 * here</A>.
	 * </P>
	 * <P>
	 * You can find a sample in the <A HREF=
	 * "https://github.com/graphql-java-generator/graphql-maven-plugin-project/blob/master/graphql-maven-plugin-samples/graphql-maven-plugin-samples-CustomTemplates-client/pom.xml">CustomTemplates
	 * client sample</A>.
	 * </P>
	 * <P>
	 * <B>Important notice:</B> Please note that the default templates may change in the future. And some of these
	 * modifications would need to be reported into the custom templates. We'll try to better expose a stable public API
	 * in the future.
	 * </P>
	 */
	private Map<String, String> templates;

	/** This is the class of the extension, that contains default value for the task attributes */
	protected Class<? extends CommonExtension> extensionClass;

	/** This is the extension, that contains default value for the task attributes */
	protected CommonExtension extension = null;

	@Inject
	public CommonTask(Class<? extends CommonExtension> extensionClazz) {
		this.extensionClass = extensionClazz;
	}

	protected <T> T getValue(T taskValue, T extensionValue) {
		return (taskValue == null) ? extensionValue : taskValue;
	}

	protected File getFileValue(String taskValue, File extensionValue) {
		return (taskValue == null) ? extensionValue : getProject().file(taskValue);
	}

	@Input
	@Override
	final public boolean isAddRelayConnections() {
		return getValue(addRelayConnections, getExtension().isAddRelayConnections());
	}

	final public void setAddRelayConnections(Boolean addRelayConnections) {
		this.addRelayConnections = addRelayConnections;
	}

	@Internal
	@Override
	final public String getDefaultTargetSchemaFileName() {
		return GenerateGraphQLSchemaConfiguration.DEFAULT_TARGET_SCHEMA_FILE_NAME;
	}

	@Internal
	@Override
	final public File getProjectDir() {
		return getProject().getProjectDir();
	}

	@Input
	@Override
	final public boolean isSkipGenerationIfSchemaHasNotChanged() {
		return getValue(skipGenerationIfSchemaHasNotChanged, getExtension().isSkipGenerationIfSchemaHasNotChanged());
	}

	final public void setSkipGenerationIfSchemaHasNotChanged(boolean skipGenerationIfSchemaHasNotChanged) {
		this.skipGenerationIfSchemaHasNotChanged = skipGenerationIfSchemaHasNotChanged;
	}

	@InputDirectory
	@Optional
	@Override
	final public File getSchemaFileFolder() {
		return getFileValue(schemaFileFolder, getExtension().getSchemaFileFolder());
	}

	final public void setSchemaFileFolder(String schemaFileFolder) {
		this.schemaFileFolder = schemaFileFolder;
	}

	@Input
	@Override
	final public String getSchemaFilePattern() {
		return getValue(schemaFilePattern, getExtension().getSchemaFilePattern());
	}

	final public void setSchemaFilePattern(String schemaFilePattern) {
		this.schemaFilePattern = schemaFilePattern;
	}

	@Input
	@Override
	final public Map<String, String> getTemplates() {
		return getValue(templates, getExtension().getTemplates());
	}

	final public void setTemplates(Map<String, String> templates) {
		this.templates = templates;
	}

	@Override
	public void logConfiguration() {
		logCommonConfiguration();
	}

	@Internal
	protected CommonExtension getExtension() {
		if (extension == null) {
			extension = getProject().getExtensions().getByType(extensionClass);
		}
		return extension;
	}

	/**
	 * This method registers to Gradle the source and resources folders in which files are generated, if any
	 */
	public void registerGeneratedFolders() {
		// No action in this class, as it manages no sources nor resources folder
	}

	// /**
	// * Let's insure that we declare in all cases the folder where sources or resources are generated
	// */
	// Commented, as these lines seems to break the build (or at least change the build behaviour): redoing a 'gradlew
	// build' with these lines uncommented prevents the test to be executed:
	// - If these lines are commented, then executing twice 'gradlew build' makes the tests be executed twice (once at
	// each build execution)
	// - If they are uncommented, then executing twice 'gradlew build' makes the tests be executed once (only for the
	// first build execution)
	//
	// This is to bad, as it seems to be the good way to declare
	//
	@Override
	@SuppressWarnings("rawtypes")
	public Task configure(Closure closure) {
		Task t = super.configure(closure);
		logger.info("[In configure] Before calling registerGeneratedFolders, for task '" + getName() + "'");
		registerGeneratedFolders();
		return t;
	}
}