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
		this.projectDir = new File(".");
		this.project = ProjectBuilder.builder().withName(PROJECT_NAME).withProjectDir(this.projectDir).build();
		this.extension = new GraphQLExtension(this.project.getProjectDir(), null);
		this.task = spy(this.project.getTasks().register("task", GraphQLGenerateCodeTask.class).get());
		this.task.setExtension(this.extension);
		doReturn(this.extension).when(this.task).getExtension();
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
				this.task.isGenerateDeprecatedRequestResponse());
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
		this.extension.setAddRelayConnections(
				!GenerateClientCodeConfiguration.DEFAULT_GENERATE_DEPRECATED_REQUEST_RESPONSE.equals("true"));

		// Go, go, go

		// Verification
		assertEquals(!GenerateClientCodeConfiguration.DEFAULT_GENERATE_DEPRECATED_REQUEST_RESPONSE.equals("true"),
				this.task.isAddRelayConnections());
	}

	/**
	 * Tests that the task values properly override the extension values it they are set
	 * 
	 * @throws IOException
	 */
	@Test
	void test_withTaskValues() throws IOException {
		// Preparation: Setting values in the task (they should override the default value of the extension)
		this.task.setAddRelayConnections(
				!GenerateClientCodeConfiguration.DEFAULT_GENERATE_DEPRECATED_REQUEST_RESPONSE.equals("true"));

		// Go, go, go

		// Verification
		assertEquals(!GenerateClientCodeConfiguration.DEFAULT_GENERATE_DEPRECATED_REQUEST_RESPONSE.equals("true"),
				this.task.isAddRelayConnections());
	}

}
