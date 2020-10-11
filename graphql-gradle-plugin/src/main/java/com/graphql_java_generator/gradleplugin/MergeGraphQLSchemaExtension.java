package com.graphql_java_generator.gradleplugin;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.gradle.api.Project;

import com.graphql_java_generator.plugin.CommonConfiguration;
import com.graphql_java_generator.plugin.Logger;
import com.graphql_java_generator.plugin.MergeSchemaConfiguration;

/**
 * Parameters for the GraphQL Gradle plugin.
 * 
 * @author EtienneSF
 *
 */
public class MergeGraphQLSchemaExtension implements MergeSchemaConfiguration, Serializable {

	private static final long serialVersionUID = 1L;

	private GradleLogger logger;

	private final Project project;

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
	public boolean addRelayConnections = CommonConfiguration.DEFAULT_ADD_RELAY_CONNECTIONS.equals("true");

	/** The encoding for the generated resource files */
	String resourceEncoding = MergeSchemaConfiguration.DEFAULT_RESOURCE_ENCODING;

	/** The folder where the graphql schema file(s) will be searched. The default schema is the main resource folder. */
	private String schemaFileFolder = MergeSchemaConfiguration.DEFAULT_SCHEMA_FILE_FOLDER;

	/**
	 * The pattern to find the graphql schema file(s). The default value is "/*.graphqls" meaning that the maven plugin
	 * will search all graphqls files in the "/src/main/resources" folder (please check also the <I>schemaFileFolder</I>
	 * plugin parameter).
	 */
	private String schemaFilePattern = MergeSchemaConfiguration.DEFAULT_SCHEMA_FILE_PATTERN;

	/** The folder where the generated GraphQL schema will be stored */
	private String targetFolder = MergeSchemaConfiguration.DEFAULT_TARGET_FOLDER;

	/**
	 * The name of the target filename, in which the schema is generated. This file is stored in the folder, defined in
	 * the <I>targetFolder</I> plugin parameter.
	 */
	private String targetSchemaFileName;

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
	private Map<String, String> templates = new HashMap<>();

	public MergeGraphQLSchemaExtension(Project project) {
		this.project = project;
		this.logger = new GradleLogger(project);
	}

	@Override
	public Logger getLog() {
		return logger;
	}

	@Override
	public boolean isAddRelayConnections() {
		return addRelayConnections;
	}

	public void setAddRelayConnections(Boolean addRelayConnections) {
		this.addRelayConnections = addRelayConnections;
	}

	@Override
	public String getPackageName() {
		// No action (this property should not exist here, will be removed in the future)
		return "not used, but may not be null!";
	}

	@Override
	public String getResourceEncoding() {
		return resourceEncoding;
	}

	@Override
	public File getSchemaFileFolder() {
		return project.file(schemaFileFolder);
	}

	@Override
	public String getSchemaFilePattern() {
		return schemaFilePattern;
	}

	@Override
	public File getTargetFolder() {
		return project.file(targetFolder);
	}

	@Override
	public String getTargetSchemaFileName() {
		return targetSchemaFileName;
	}

	@Override
	public Map<String, String> getTemplates() {
		return templates;
	}

	public void setLogger(GradleLogger logger) {
		this.logger = logger;
	}

	public void setPackageName(String packageName) {
		// No action (this property should not exist here, will be removed in the future)
	}

	public void setResourceEncoding(String resourceEncoding) {
		this.resourceEncoding = resourceEncoding;
	}

	public void setSchemaFileFolder(String schemaFileFolder) {
		this.schemaFileFolder = schemaFileFolder;
	}

	public void setSchemaFilePattern(String schemaFilePattern) {
		this.schemaFilePattern = schemaFilePattern;
	}

	public void setTargetFolder(String targetFolder) {
		// Let's create the folder now, so that it exists when if any other task needs it, during configuration time
		project.file(targetFolder).mkdirs();

		this.targetFolder = targetFolder;
	}

	public void setTargetSchemaFileName(String targetSchemaFileName) {
		this.targetSchemaFileName = targetSchemaFileName;
	}

	public void setTemplates(Map<String, String> templates) {
		this.templates = templates;
	}

}
