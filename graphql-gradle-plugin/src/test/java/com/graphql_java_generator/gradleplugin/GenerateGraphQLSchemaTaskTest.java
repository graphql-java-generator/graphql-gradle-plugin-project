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
	Project project;
	GenerateGraphQLSchemaExtension extension;

	@Mock
	GenerateGraphQLSchemaTask task;

	@BeforeEach
	void setup() {
		this.projectDir = new File(".");
		this.project = ProjectBuilder.builder().withName(PROJECT_NAME).withProjectDir(this.projectDir).build();
		this.extension = new GenerateGraphQLSchemaExtension(this.project.getProjectDir());
		this.task = spy(this.project.getTasks().register("task", GenerateGraphQLSchemaTask.class).get());
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
		assertEquals(GenerateGraphQLSchemaConfiguration.DEFAULT_RESOURCE_ENCODING, this.task.getResourceEncoding());
		assertEquals(
				new File(this.projectDir, GenerateGraphQLSchemaConfiguration.DEFAULT_TARGET_FOLDER).getCanonicalPath(),
				this.task.getTargetFolder().getCanonicalPath());
		assertEquals(GenerateGraphQLSchemaConfiguration.DEFAULT_TARGET_SCHEMA_FILE_NAME,
				this.task.getTargetSchemaFileName());
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
		this.extension.setResourceEncoding("UTF 666");
		this.extension.setTargetFolder("anotherFolder");
		this.extension.setTargetSchemaFileName("a.file.txt");

		// Go, go, go

		// Verification
		assertEquals("UTF 666", this.task.getResourceEncoding());
		assertEquals(new File(this.projectDir, "anotherFolder").getCanonicalPath(),
				this.task.getTargetFolder().getCanonicalPath());
		assertEquals("a.file.txt", this.task.getTargetSchemaFileName());
	}

	/**
	 * Tests that the task values properly override the extension values it they are set
	 * 
	 * @throws IOException
	 */
	@Test
	void test_withTaskValues() throws IOException {
		// Preparation
		this.extension.setResourceEncoding("UTF 666");
		this.extension.setTargetFolder("anotherFolder");
		this.extension.setTargetSchemaFileName("a.file.txt");

		// Go, go, go

		// Verification
		assertEquals("UTF 666", this.task.getResourceEncoding());
		assertEquals(new File(this.projectDir, "anotherFolder").getCanonicalPath(),
				this.task.getTargetFolder().getCanonicalPath());
		assertEquals("a.file.txt", this.task.getTargetSchemaFileName());
	}

}
