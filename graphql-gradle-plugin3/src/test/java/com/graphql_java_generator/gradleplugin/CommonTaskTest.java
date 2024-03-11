package com.graphql_java_generator.gradleplugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.graphql_java_generator.plugin.conf.CommonConfiguration;
import com.graphql_java_generator.plugin.conf.GenerateGraphQLSchemaConfiguration;
import com.graphql_java_generator.plugin.conf.GraphQLConfiguration;

public class CommonTaskTest {

	static final String PROJECT_NAME = "A Dummy project";
	File projectDir;
	Project project;
	CommonExtension extension;

	@Mock
	CommonTask task;

	@BeforeEach
	void setup() {
		this.projectDir = new File(".");
		this.project = ProjectBuilder.builder().withName(PROJECT_NAME).withProjectDir(this.projectDir).build();
		this.extension = new CommonExtension(this.project.getProjectDir());
		this.task = spy(this.project.getTasks().register("task", CommonTask.class, this.extension).get());
		doReturn(this.extension).when(this.task).getExtension();
	}

	@Test
	void test_getValue() {
		assertEquals((Object) null, this.task.getValue(null, null));
		assertEquals("Test1", this.task.getValue(null, "Test1"));
		assertEquals("Test2", this.task.getValue("Test2", "Test1"));
	}

	/**
	 * Tests that the default plugin values are properly copied into the task properties, when no special specific is
	 * done
	 * 
	 * @throws IOException
	 */
	@SuppressWarnings("deprecation")
	@Test
	void test_noExtension() throws IOException {
		assertEquals(CommonConfiguration.DEFAULT_ADD_RELAY_CONNECTIONS.equals("true"),
				this.task.isAddRelayConnections());
		assertEquals(GenerateGraphQLSchemaConfiguration.DEFAULT_TARGET_SCHEMA_FILE_NAME,
				this.task.getDefaultTargetSchemaFileName());
		assertEquals(this.projectDir.getCanonicalPath(), this.task.getProjectDir().getCanonicalPath());
		assertEquals(CommonConfiguration.DEFAULT_SKIP_GENERATION_IF_SCHEMA_HAS_NOT_CHANGED.equals("true"),
				this.task.isSkipGenerationIfSchemaHasNotChanged());
		assertEquals(new File(this.projectDir, GraphQLConfiguration.DEFAULT_SCHEMA_FILE_FOLDER).getCanonicalPath(),
				this.task.getSchemaFileFolder().getCanonicalPath());
		assertEquals(GraphQLConfiguration.DEFAULT_SCHEMA_FILE_PATTERN, this.task.getSchemaFilePattern());
		assertEquals(0, this.task.getTemplates().size());
	}

	/**
	 * Tests that the extension values are properly copied into the task properties, when the extension attributes have
	 * been set
	 * 
	 * @throws IOException
	 */
	@SuppressWarnings("deprecation")
	@Test
	void test_withExtensionValues() throws IOException {
		// Preparation
		this.extension.setAddRelayConnections(!CommonConfiguration.DEFAULT_ADD_RELAY_CONNECTIONS.equals("true"));
		this.extension.setSkipGenerationIfSchemaHasNotChanged(
				!CommonConfiguration.DEFAULT_SKIP_GENERATION_IF_SCHEMA_HAS_NOT_CHANGED.equals("true"));
		this.extension.setSchemaFileFolder("aFolder");
		this.extension.setSchemaFilePattern("a pattern");
		this.extension.setTemplates(null);

		// Go, go, go

		// Verification
		assertEquals(!CommonConfiguration.DEFAULT_ADD_RELAY_CONNECTIONS.equals("true"),
				this.task.isAddRelayConnections());
		assertEquals(GenerateGraphQLSchemaConfiguration.DEFAULT_TARGET_SCHEMA_FILE_NAME,
				this.task.getDefaultTargetSchemaFileName());
		assertEquals(this.projectDir.getCanonicalPath(), this.task.getProjectDir().getCanonicalPath());
		assertEquals(!CommonConfiguration.DEFAULT_SKIP_GENERATION_IF_SCHEMA_HAS_NOT_CHANGED.equals("true"),
				this.task.isSkipGenerationIfSchemaHasNotChanged());
		assertEquals(new File(this.projectDir, "aFolder").getCanonicalPath(),
				this.task.getSchemaFileFolder().getCanonicalPath());
		assertEquals("a pattern", this.task.getSchemaFilePattern());
		assertEquals(null, this.task.getTemplates());
	}

	/**
	 * Tests that the task values properly override the extension values it they are set
	 * 
	 * @throws IOException
	 */
	@SuppressWarnings("deprecation")
	@Test
	void test_withTaskValues() throws IOException {
		// Preparation: Setting values in the task (they should override the default value of the extension)
		this.task.setAddRelayConnections(!CommonConfiguration.DEFAULT_ADD_RELAY_CONNECTIONS.equals("true"));
		this.task.setSkipGenerationIfSchemaHasNotChanged(
				!CommonConfiguration.DEFAULT_SKIP_GENERATION_IF_SCHEMA_HAS_NOT_CHANGED.equals("true"));
		this.task.setSchemaFileFolder("aFolder");
		this.task.setSchemaFilePattern("a pattern");
		Map<String, String> templates = new HashMap<>();
		this.task.setTemplates(templates);

		// Go, go, go

		// Verification
		assertEquals(!CommonConfiguration.DEFAULT_ADD_RELAY_CONNECTIONS.equals("true"),
				this.task.isAddRelayConnections());
		assertEquals(GenerateGraphQLSchemaConfiguration.DEFAULT_TARGET_SCHEMA_FILE_NAME,
				this.task.getDefaultTargetSchemaFileName());
		assertEquals(this.projectDir.getCanonicalPath(), this.task.getProjectDir().getCanonicalPath());
		assertEquals(!CommonConfiguration.DEFAULT_SKIP_GENERATION_IF_SCHEMA_HAS_NOT_CHANGED.equals("true"),
				this.task.isSkipGenerationIfSchemaHasNotChanged());
		assertEquals(new File(this.projectDir, "aFolder").getCanonicalPath(),
				this.task.getSchemaFileFolder().getCanonicalPath());
		assertEquals("a pattern", this.task.getSchemaFilePattern());
		assertEquals(templates, this.task.getTemplates());
	}

}
