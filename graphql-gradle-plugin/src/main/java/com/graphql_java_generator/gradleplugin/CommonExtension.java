/**
 * 
 */
package com.graphql_java_generator.gradleplugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.gradle.api.file.ProjectLayout;

import com.graphql_java_generator.plugin.conf.CommonConfiguration;
import com.graphql_java_generator.plugin.conf.GraphQLConfiguration;

/**
 * <P>
 * This class contain the parameters that are common to all Gradle Extensions for this plugin, that is the parameters
 * that are common to all the tasks of this plugin. The extensions are used here to define the default values for the
 * task parameters
 * </P>
 * <P>
 * This avoids to redeclare each common parameter in each Extension, including its comment. When a comment is updated,
 * only one update is necessary, instead of updating it in each Extension.
 * </P>
 * 
 * @author etienne-sf
 */
public class CommonExtension implements CommonConfiguration {

	private boolean initialized = false;

	/** The layout of the current project. This allowed to create an instance of {@link File} from a relative path */
	protected final ProjectLayout projectLayout;

	private String enumPrefix = CommonConfiguration.DEFAULT_PREFIX;
	private String enumSuffix = CommonConfiguration.DEFAULT_SUFFIX;
	private String inputPrefix = CommonConfiguration.DEFAULT_PREFIX;
	private String inputSuffix = CommonConfiguration.DEFAULT_SUFFIX;
	private String interfacePrefix = CommonConfiguration.DEFAULT_PREFIX;
	private String interfaceSuffix = CommonConfiguration.DEFAULT_SUFFIX;
	private String jsonGraphqlSchemaFilename = CommonConfiguration.DEFAULT_JSON_GRAPHQL_SCHEMA_FILE;
	private Integer maxTokens = Integer.parseInt(CommonConfiguration.DEFAULT_MAX_TOKENS);
	private boolean addRelayConnections = CommonConfiguration.DEFAULT_ADD_RELAY_CONNECTIONS.equals("true");
	private String schemaFileFolder = GraphQLConfiguration.DEFAULT_SCHEMA_FILE_FOLDER;
	private String schemaFilePattern = GraphQLConfiguration.DEFAULT_SCHEMA_FILE_PATTERN;
	private boolean skipGenerationIfSchemaHasNotChanged = CommonConfiguration.DEFAULT_SKIP_GENERATION_IF_SCHEMA_HAS_NOT_CHANGED
			.equals("true");
	private Map<String, String> templates = new HashMap<>();
	private String typePrefix = CommonConfiguration.DEFAULT_PREFIX;
	private String typeSuffix = CommonConfiguration.DEFAULT_SUFFIX;
	private String unionPrefix = CommonConfiguration.DEFAULT_PREFIX;
	private String unionSuffix = CommonConfiguration.DEFAULT_SUFFIX;

	public CommonExtension(ProjectLayout projectLayout) {
		this.projectLayout = projectLayout;
	}

	/**
	 * An optional prefix to add to the classnames of the generated java classes for GraphQL enums. The prefix is added
	 * at the beginning of the java classname, and must be compatible with java naming rules (no space, dot, comma,
	 * etc.)
	 */
	@Override
	public String getEnumPrefix() {
		return enumPrefix;
	}

	public void setEnumPrefix(String enumPrefix) {
		this.enumPrefix = enumPrefix;
	}

	/**
	 * An optional suffix to add to the classnames of the generated java classes for GraphQL enums. The suffix is added
	 * at the end of the java classname, and must be compatible with java naming rules (no space, dot, comma, etc.)
	 */
	@Override
	public String getEnumSuffix() {
		return enumSuffix;
	}

	public void setEnumSuffix(String enumSuffix) {
		this.enumSuffix = enumSuffix;
	}

	public boolean isInitialized() {
		return initialized;
	}

	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

	/**
	 * An optional prefix to add to the classnames of the generated java classes for GraphQL input objects. The prefix
	 * is added at the beginning of the java classname, and must be compatible with java naming rules (no space, dot,
	 * comma, etc.)
	 */
	@Override
	public String getInputPrefix() {
		return inputPrefix;
	}

	public void setInputPrefix(String inputPrefix) {
		this.inputPrefix = inputPrefix;
	}

	/**
	 * An optional suffix to add to the classnames of the generated java classes for GraphQL input objects. The suffix
	 * is added at the end of the java classname, and must be compatible with java naming rules (no space, dot, comma,
	 * etc.)
	 */
	@Override
	public String getInputSuffix() {
		return inputSuffix;
	}

	public void setInputSuffix(String inputSuffix) {
		this.inputSuffix = inputSuffix;
	}

	@Override
	public Integer getMaxTokens() {
		return maxTokens;
	}

	public void setMaxTokens(Integer maxTokens) {
		this.maxTokens = maxTokens;
	}

	@Override
	public File getProjectBuildDir() {
		return projectLayout.getBuildDirectory().getAsFile().get();
	}

	@Override
	public File getProjectDir() {
		return projectLayout.getProjectDirectory().getAsFile();
	}

	@Override
	public File getSchemaFileFolder() {
		return new File(getProjectDir(), schemaFileFolder);
	}

	public final void setSchemaFileFolder(String schemaFileFolder) {
		this.schemaFileFolder = schemaFileFolder;
	}

	@Override
	public String getSchemaFilePattern() {
		return schemaFilePattern;
	}

	public final void setSchemaFilePattern(String schemaFilePattern) {
		this.schemaFilePattern = schemaFilePattern;
	}

	@Override
	public Map<String, String> getTemplates() {
		return templates;
	}

	public final void setTemplates(Map<String, String> templates) {
		this.templates = templates;
	}

	/**
	 * An optional prefix to add to the classnames of the generated java classes for GraphQL types. The prefix is added
	 * at the beginning of the java classname, and must be compatible with java naming rules (no space, dot, comma,
	 * etc.)
	 */
	@Override
	public String getTypePrefix() {
		return typePrefix;
	}

	public void setTypePrefix(String typePrefix) {
		this.typePrefix = typePrefix;
	}

	/**
	 * An optional suffix to add to the classnames of the generated java classes for GraphQL types. The suffix is added
	 * at the end of the java classname, and must be compatible with java naming rules (no space, dot, comma, etc.)
	 */
	@Override
	public String getTypeSuffix() {
		return typeSuffix;
	}

	public void setTypeSuffix(String typeSuffix) {
		this.typeSuffix = typeSuffix;
	}

	/**
	 * An optional prefix to add to the classnames of the generated java classes for GraphQL unions. The prefix is added
	 * at the beginning of the java classname, and must be compatible with java naming rules (no space, dot, comma,
	 * etc.)
	 */
	@Override
	public String getUnionPrefix() {
		return unionPrefix;
	}

	public void setUnionPrefix(String unionPrefix) {
		this.unionPrefix = unionPrefix;
	}

	/**
	 * An optional suffix to add to the classnames of the generated java classes for GraphQL unions. The suffix is added
	 * at the end of the java classname, and must be compatible with java naming rules (no space, dot, comma, etc.)
	 */
	@Override
	public String getUnionSuffix() {
		return unionSuffix;
	}

	public void setUnionSuffix(String unionSuffix) {
		this.unionSuffix = unionSuffix;
	}

	/**
	 * An optional prefix to add to the classnames of the generated java classes for GraphQL interfaces. The prefix is
	 * added at the beginning of the java classname, and must be compatible with java naming rules (no space, dot,
	 * comma, etc.)
	 */
	@Override
	public String getInterfacePrefix() {
		return interfacePrefix;
	}

	public void setInterfacePrefix(String interfacePrefix) {
		this.interfacePrefix = interfacePrefix;
	}

	/**
	 * An optional suffix to add to the classnames of the generated java classes for GraphQL interfaces. The suffix is
	 * added at the end of the java classname, and must be compatible with java naming rules (no space, dot, comma,
	 * etc.)
	 */
	@Override
	public String getInterfaceSuffix() {
		return interfaceSuffix;
	}

	public void setInterfaceSuffix(String interfaceSuffix) {
		this.interfaceSuffix = interfaceSuffix;
	}

	/**
	 * <p>
	 * If defined, the plugin loads the GraphQL schema from this json file. This allows to generate the code from the
	 * result of a GraphQL introspection query executed against an existing GraphQL server, for instance if you don't
	 * have its GraphQL schema file.
	 * </p>
	 * <p>
	 * This json file should have been retrieved by the full introspection query. You can find the introspection query
	 * from the <code>getIntrospectionQuery</code> of the
	 * <a href="https://github.com/graphql/graphql-js/blob/main/src/utilities/getIntrospectionQuery.ts">graphql-js</a>
	 * or from this <a href=
	 * "https://github.com/graphql-java/graphql-java/blob/master/src/main/java/graphql/introspection/IntrospectionQuery.java">graphql-java</a>
	 * class. You then have to run it against the GraphQL server, and store the response into a schema.json file.
	 * </p>
	 * 
	 * @return
	 */
	@Override
	public String getJsonGraphqlSchemaFilename() {
		return jsonGraphqlSchemaFilename;
	}

	public void setJsonGraphqlSchemaFilename(String jsonGraphqlSchemaFilename) {
		this.jsonGraphqlSchemaFilename = jsonGraphqlSchemaFilename;
	}

	@Override
	public boolean isAddRelayConnections() {
		return addRelayConnections;
	}

	public final void setAddRelayConnections(boolean addRelayConnections) {
		this.addRelayConnections = addRelayConnections;
	}

	@Override
	@Deprecated
	public boolean isSkipGenerationIfSchemaHasNotChanged() {
		return skipGenerationIfSchemaHasNotChanged;
	}

	public final void setSkipGenerationIfSchemaHasNotChanged(boolean skipGenerationIfSchemaHasNotChanged) {
		this.skipGenerationIfSchemaHasNotChanged = skipGenerationIfSchemaHasNotChanged;
	}

	@Override
	public void logConfiguration() {
		// No action in the extension
	}

}
