/**
 * 
 */
package com.graphql_java_generator.gradleplugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.gradle.api.Project;

import com.graphql_java_generator.plugin.conf.CommonConfiguration;
import com.graphql_java_generator.plugin.conf.GraphQLConfiguration;

/**
 * <P>
 * This class contain the parameters that are common to all Gradle Extensions for this plugin, that is the parameters
 * that are common to all the tasks of this plugin.
 * </P>
 * <P>
 * This avoids to redeclare each common parameter in each Extension, including its comment. When a comment is updated,
 * only one update is necessary, instead of updating it in each Extension.
 * </P>
 * 
 * @author etienne-sf
 */
public class CommonExtension implements CommonConfiguration {

	protected final Project project;

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

	/** The folder where the graphql schema file(s) will be searched. The default schema is the main resource folder. */
	private String schemaFileFolder = GraphQLConfiguration.DEFAULT_SCHEMA_FILE_FOLDER;

	/**
	 * The pattern to find the graphql schema file(s). The default value is "/*.graphqls" meaning that the maven plugin
	 * will search all graphqls files in the "/src/main/resources" folder (please check also the <I>schemaFileFolder</I>
	 * plugin parameter).
	 */
	private String schemaFilePattern = GraphQLConfiguration.DEFAULT_SCHEMA_FILE_PATTERN;

	/**
	 * <P>
	 * This parameter is now <B><I>deprecated</I></B>: it's value used in the plugin is always true, that is: if the
	 * generated sources or resources are older than the GraphQL schema file(s), then there is no source or resource
	 * generation. In clear, the source and resource generation is always executed only if the provided input (GraphQL
	 * schema...) has been updated since the last plugin execution.
	 * </P>
	 */
	public boolean skipGenerationIfSchemaHasNotChanged = CommonConfiguration.DEFAULT_ADD_RELAY_CONNECTIONS
			.equals("true");

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

	public CommonExtension(Project project) {
		this.project = project;
	}

	@Override
	public boolean isAddRelayConnections() {
		return addRelayConnections;
	}

	public void setAddRelayConnections(Boolean addRelayConnections) {
		this.addRelayConnections = addRelayConnections;
	}

	@Override
	public File getProjectDir() {
		return project.getProjectDir();
	}

	@Override
	public boolean isSkipGenerationIfSchemaHasNotChanged() {
		return skipGenerationIfSchemaHasNotChanged;
	}

	public void setSkipGenerationIfSchemaHasNotChanged(boolean skipGenerationIfSchemaHasNotChanged) {
		this.skipGenerationIfSchemaHasNotChanged = skipGenerationIfSchemaHasNotChanged;
	}

	@Override
	public File getSchemaFileFolder() {
		return project.file(schemaFileFolder);
	}

	public void setSchemaFileFolder(String schemaFileFolder) {
		this.schemaFileFolder = schemaFileFolder;
	}

	@Override
	public String getSchemaFilePattern() {
		return schemaFilePattern;
	}

	public void setSchemaFilePattern(String schemaFilePattern) {
		this.schemaFilePattern = schemaFilePattern;
	}

	@Override
	public Map<String, String> getTemplates() {
		return templates;
	}

	public void setTemplates(Map<String, String> templates) {
		this.templates = templates;
	}

	@Override
	public void logConfiguration() {
		logCommonConfiguration();
	}
}
