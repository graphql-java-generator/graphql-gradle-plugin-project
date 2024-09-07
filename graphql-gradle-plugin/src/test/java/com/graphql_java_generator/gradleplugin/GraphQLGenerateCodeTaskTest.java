package com.graphql_java_generator.gradleplugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.io.File;
import java.io.IOException;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.graphql_java_generator.plugin.conf.GenerateClientCodeConfiguration;

public class GraphQLGenerateCodeTaskTest {

	static final String PROJECT_NAME = "A Dummy project";
	File projectDir;
	Project project;
	GraphQLExtension extension;

	@Mock
	GraphQLGenerateCodeTask task;

	@BeforeEach
	void setup() {
		projectDir = new File(".");
		project = ProjectBuilder.builder().withName(PROJECT_NAME).withProjectDir(projectDir).build();
		extension = new GraphQLExtension(project.getLayout(), null);
		task = spy(project.getTasks().register("task", GraphQLGenerateCodeTask.class).get());
		task.setExtension(extension);
		doReturn(extension).when(task).getExtension();
	}

	/**
	 * Tests that the default plugin values are properly copied into the task properties, when no special specific is
	 * done
	 * 
	 * @throws IOException
	 */
	@Test
	void test_noExtension() throws IOException {
		assertEquals(GenerateClientCodeConfiguration.DEFAULT_GENERATE_DEPRECATED_REQUEST_RESPONSE.equals("true"),
				task.isGenerateDeprecatedRequestResponse());
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
		extension.setAddRelayConnections(
				!GenerateClientCodeConfiguration.DEFAULT_GENERATE_DEPRECATED_REQUEST_RESPONSE.equals("true"));

		// Go, go, go

		// Verification
		assertEquals(!GenerateClientCodeConfiguration.DEFAULT_GENERATE_DEPRECATED_REQUEST_RESPONSE.equals("true"),
				task.isAddRelayConnections());
	}

	/**
	 * Tests that the task values properly override the extension values it they are set
	 * 
	 * @throws IOException
	 */
	@Test
	void test_withTaskValues() throws IOException {
		// Preparation: Setting values in the task (they should override the default value of the extension)
		task.setAddRelayConnections(
				!GenerateClientCodeConfiguration.DEFAULT_GENERATE_DEPRECATED_REQUEST_RESPONSE.equals("true"));

		// Go, go, go

		// Verification
		assertEquals(!GenerateClientCodeConfiguration.DEFAULT_GENERATE_DEPRECATED_REQUEST_RESPONSE.equals("true"),
				task.isAddRelayConnections());
	}

}
