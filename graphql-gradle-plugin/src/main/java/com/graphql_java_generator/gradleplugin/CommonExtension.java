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

	final protected Project project;

	private int maxTokens = Integer.parseInt(CommonConfiguration.DEFAULT_MAX_TOKENS);
	private boolean addRelayConnections = CommonConfiguration.DEFAULT_ADD_RELAY_CONNECTIONS.equals("true");
	private String schemaFileFolder = GraphQLConfiguration.DEFAULT_SCHEMA_FILE_FOLDER;
	private String schemaFilePattern = GraphQLConfiguration.DEFAULT_SCHEMA_FILE_PATTERN;
	public boolean skipGenerationIfSchemaHasNotChanged = CommonConfiguration.DEFAULT_SKIP_GENERATION_IF_SCHEMA_HAS_NOT_CHANGED
			.equals("true");
	private Map<String, String> templates = new HashMap<>();

	public CommonExtension(Project project) {
		this.project = project;
	}

	@Override
	public int getMaxTokens() {
		return maxTokens;
	}

	public void setMaxTokens(int maxTokens) {
		this.maxTokens = maxTokens;
	}

	@Override
	public File getProjectDir() {
		return project.getProjectDir();
	}

	@Override
	public File getSchemaFileFolder() {
		return project.file(schemaFileFolder);
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

	@Override
	public boolean isAddRelayConnections() {
		return addRelayConnections;
	}

	public final void setAddRelayConnections(boolean addRelayConnections) {
		this.addRelayConnections = addRelayConnections;
	}

	@Override
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
