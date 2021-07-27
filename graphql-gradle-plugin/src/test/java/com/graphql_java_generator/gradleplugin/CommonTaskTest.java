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

class CommonTaskTest {

	static final String PROJECT_NAME = "A Dummy project";
	File projectDir;
	Project project;
	CommonExtension extension;

	@Mock
	CommonTask task;

	@BeforeEach
	void setup() {
		projectDir = new File(".");
		project = ProjectBuilder.builder().withName(PROJECT_NAME).withProjectDir(projectDir).build();
		task = spy(project.getTasks().register("task", CommonTask.class, CommonExtension.class).get());
		extension = new CommonExtension(project);
		doReturn(extension).when(task).getExtension();
	}

	@Test
	void test_getValue() {
		assertEquals((Object) null, task.getValue(null, null));
		assertEquals("Test1", task.getValue(null, "Test1"));
		assertEquals("Test2", task.getValue("Test2", "Test1"));
	}

	@Test
	void test_getFileValue() throws IOException {
		assertEquals((String) null, task.getFileValue(null, null));
		assertEquals(projectDir, task.getFileValue(null, projectDir));

		assertEquals(new File(projectDir, "Test2").getCanonicalPath(),
				task.getFileValue("Test2", projectDir).getCanonicalPath());
	}

	/**
	 * Tests that the default plugin values are properly copied into the task properties, when no special specific is
	 * done
	 * 
	 * @throws IOException
	 */
	@Test
	void test_noExtension() throws IOException {
		assertEquals(CommonConfiguration.DEFAULT_ADD_RELAY_CONNECTIONS.equals("true"), task.isAddRelayConnections());
		assertEquals(GenerateGraphQLSchemaConfiguration.DEFAULT_TARGET_SCHEMA_FILE_NAME,
				task.getDefaultTargetSchemaFileName());
		assertEquals(projectDir.getCanonicalPath(), task.getProjectDir().getCanonicalPath());
		assertEquals(CommonConfiguration.DEFAULT_SKIP_GENERATION_IF_SCHEMA_HAS_NOT_CHANGED.equals("true"),
				task.isSkipGenerationIfSchemaHasNotChanged());
		assertEquals(new File(projectDir, GraphQLConfiguration.DEFAULT_SCHEMA_FILE_FOLDER).getCanonicalPath(),
				task.getSchemaFileFolder().getCanonicalPath());
		assertEquals(GraphQLConfiguration.DEFAULT_SCHEMA_FILE_PATTERN, task.getSchemaFilePattern());
		assertEquals(0, task.getTemplates().size());
	}

	/**
	 * Tests that the extension values are properly copied into the task properties, when the extension attributes have
	 * been set
	 * 
	 * @throws IOException
	 */
	@Test
	void test_withExtensionValues() throws IOException {
		// Preparation
		extension.setAddRelayConnections(!CommonConfiguration.DEFAULT_ADD_RELAY_CONNECTIONS.equals("true"));
		extension.setSkipGenerationIfSchemaHasNotChanged(
				!CommonConfiguration.DEFAULT_SKIP_GENERATION_IF_SCHEMA_HAS_NOT_CHANGED.equals("true"));
		extension.setSchemaFileFolder("aFolder");
		extension.setSchemaFilePattern("a pattern");
		extension.setTemplates(null);

		// Go, go, go

		// Verification
		assertEquals(!CommonConfiguration.DEFAULT_ADD_RELAY_CONNECTIONS.equals("true"), task.isAddRelayConnections());
		assertEquals(GenerateGraphQLSchemaConfiguration.DEFAULT_TARGET_SCHEMA_FILE_NAME,
				task.getDefaultTargetSchemaFileName());
		assertEquals(projectDir.getCanonicalPath(), task.getProjectDir().getCanonicalPath());
		assertEquals(!CommonConfiguration.DEFAULT_SKIP_GENERATION_IF_SCHEMA_HAS_NOT_CHANGED.equals("true"),
				task.isSkipGenerationIfSchemaHasNotChanged());
		assertEquals(new File(projectDir, "aFolder").getCanonicalPath(), task.getSchemaFileFolder().getCanonicalPath());
		assertEquals("a pattern", task.getSchemaFilePattern());
		assertEquals(null, task.getTemplates());
	}

	/**
	 * Tests that the task values properly override the extension values it they are set
	 * 
	 * @throws IOException
	 */
	@Test
	void test_withTaskValues() throws IOException {
		// Preparation: Setting values in the task (they should override the default value of the extension)
		task.setAddRelayConnections(!CommonConfiguration.DEFAULT_ADD_RELAY_CONNECTIONS.equals("true"));
		task.setSkipGenerationIfSchemaHasNotChanged(
				!CommonConfiguration.DEFAULT_SKIP_GENERATION_IF_SCHEMA_HAS_NOT_CHANGED.equals("true"));
		task.setSchemaFileFolder("aFolder");
		task.setSchemaFilePattern("a pattern");
		Map<String, String> templates = new HashMap<>();
		task.setTemplates(templates);

		// Go, go, go

		// Verification
		assertEquals(!CommonConfiguration.DEFAULT_ADD_RELAY_CONNECTIONS.equals("true"), task.isAddRelayConnections());
		assertEquals(GenerateGraphQLSchemaConfiguration.DEFAULT_TARGET_SCHEMA_FILE_NAME,
				task.getDefaultTargetSchemaFileName());
		assertEquals(projectDir.getCanonicalPath(), task.getProjectDir().getCanonicalPath());
		assertEquals(!CommonConfiguration.DEFAULT_SKIP_GENERATION_IF_SCHEMA_HAS_NOT_CHANGED.equals("true"),
				task.isSkipGenerationIfSchemaHasNotChanged());
		assertEquals(new File(projectDir, "aFolder").getCanonicalPath(), task.getSchemaFileFolder().getCanonicalPath());
		assertEquals("a pattern", task.getSchemaFilePattern());
		assertEquals(templates, task.getTemplates());
	}

}
