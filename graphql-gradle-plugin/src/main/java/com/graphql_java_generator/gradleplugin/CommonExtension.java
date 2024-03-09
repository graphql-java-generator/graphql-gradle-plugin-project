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

	/** The current Gradle project */
	final protected Project project;

	private boolean initialized = false;

	private String enumPrefix = CommonConfiguration.DEFAULT_PREFIX;
	private String enumSuffix = CommonConfiguration.DEFAULT_SUFFIX;
	private String inputPrefix = CommonConfiguration.DEFAULT_PREFIX;
	private String inputSuffix = CommonConfiguration.DEFAULT_SUFFIX;
	private String interfacePrefix = CommonConfiguration.DEFAULT_PREFIX;
	private String interfaceSuffix = CommonConfiguration.DEFAULT_SUFFIX;
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

	public CommonExtension(Project project) {
		this.project = project;
	}

	/**
	 * An optional prefix to add to the classnames of the generated java classes for GraphQL enums. The prefix is added
	 * at the beginning of the java classname, and must be compatible with java naming rules (no space, dot, comma,
	 * etc.)
	 */
	@Override
	public String getEnumPrefix() {
		return this.enumPrefix;
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
		return this.enumSuffix;
	}

	public void setEnumSuffix(String enumSuffix) {
		this.enumSuffix = enumSuffix;
	}

	public boolean isInitialized() {
		return this.initialized;
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
		return this.inputPrefix;
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
		return this.inputSuffix;
	}

	public void setInputSuffix(String inputSuffix) {
		this.inputSuffix = inputSuffix;
	}

	@Override
	public Integer getMaxTokens() {
		return this.maxTokens;
	}

	public void setMaxTokens(Integer maxTokens) {
		this.maxTokens = maxTokens;
	}

	@Override
	public File getProjectDir() {
		return this.project.getProjectDir();
	}

	@Override
	public File getSchemaFileFolder() {
		return this.project.file(this.schemaFileFolder);
	}

	public final void setSchemaFileFolder(String schemaFileFolder) {
		this.schemaFileFolder = schemaFileFolder;
	}

	@Override
	public String getSchemaFilePattern() {
		return this.schemaFilePattern;
	}

	public final void setSchemaFilePattern(String schemaFilePattern) {
		this.schemaFilePattern = schemaFilePattern;
	}

	@Override
	public Map<String, String> getTemplates() {
		return this.templates;
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
		return this.typePrefix;
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
		return this.typeSuffix;
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
		return this.unionPrefix;
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
		return this.unionSuffix;
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
		return this.interfacePrefix;
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
		return this.interfaceSuffix;
	}

	public void setInterfaceSuffix(String interfaceSuffix) {
		this.interfaceSuffix = interfaceSuffix;
	}

	@Override
	public boolean isAddRelayConnections() {
		return this.addRelayConnections;
	}

	public final void setAddRelayConnections(boolean addRelayConnections) {
		this.addRelayConnections = addRelayConnections;
	}

	@Override
	@Deprecated
	public boolean isSkipGenerationIfSchemaHasNotChanged() {
		return this.skipGenerationIfSchemaHasNotChanged;
	}

	public final void setSkipGenerationIfSchemaHasNotChanged(boolean skipGenerationIfSchemaHasNotChanged) {
		this.skipGenerationIfSchemaHasNotChanged = skipGenerationIfSchemaHasNotChanged;
	}

	@Override
	public void logConfiguration() {
		// No action in the extension
	}

}
