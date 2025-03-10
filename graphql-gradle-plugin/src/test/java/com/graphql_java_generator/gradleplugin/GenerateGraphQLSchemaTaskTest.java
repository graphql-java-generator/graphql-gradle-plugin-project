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

import com.graphql_java_generator.plugin.conf.GenerateGraphQLSchemaConfiguration;

public class GenerateGraphQLSchemaTaskTest {

	static final String PROJECT_NAME = "A Dummy project";
	File projectDir;
	File buildDir;
	Project project;
	GenerateGraphQLSchemaExtension extension;

	@Mock
	GenerateGraphQLSchemaTask task;

	@BeforeEach
	void setup() {
		projectDir = new File(".");
		buildDir = new File(projectDir, "build");
		project = ProjectBuilder.builder().withName(PROJECT_NAME).withProjectDir(projectDir).build();
		extension = new GenerateGraphQLSchemaExtension(project.getLayout());
		task = spy(project.getTasks().register("task", GenerateGraphQLSchemaTask.class).get());
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
		assertEquals(GenerateGraphQLSchemaConfiguration.DEFAULT_RESOURCE_ENCODING, task.getResourceEncoding());
		assertEquals(new File(buildDir, GenerateGraphQLSchemaConfiguration.DEFAULT_TARGET_FOLDER).getCanonicalPath(),
				task.getTargetFolder().getCanonicalPath());
		assertEquals(GenerateGraphQLSchemaConfiguration.DEFAULT_TARGET_SCHEMA_FILE_NAME,
				task.getTargetSchemaFileName());
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
		extension.setResourceEncoding("UTF 666");
		extension.setTargetFolder("anotherFolder");
		extension.setTargetSchemaFileName("a.file.txt");

		// Go, go, go

		// Verification
		assertEquals("UTF 666", task.getResourceEncoding());
		assertEquals(new File(projectDir, "anotherFolder").getCanonicalPath(),
				task.getTargetFolder().getCanonicalPath());
		assertEquals("a.file.txt", task.getTargetSchemaFileName());
	}

	/**
	 * Tests that the task values properly override the extension values it they are set
	 * 
	 * @throws IOException
	 */
	@Test
	void test_withTaskValues() throws IOException {
		// Preparation
		extension.setResourceEncoding("UTF 666");
		extension.setTargetFolder("anotherFolder");
		extension.setTargetSchemaFileName("a.file.txt");

		// Go, go, go

		// Verification
		assertEquals("UTF 666", task.getResourceEncoding());
		assertEquals(new File(projectDir, "anotherFolder").getCanonicalPath(),
				task.getTargetFolder().getCanonicalPath());
		assertEquals("a.file.txt", task.getTargetSchemaFileName());
	}

}
