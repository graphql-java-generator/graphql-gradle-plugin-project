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

import com.graphql_java_generator.plugin.conf.GenerateServerCodeConfiguration;

public class GenerateServerCodeTaskTest {

	static final String PROJECT_NAME = "A Dummy project";
	File projectDir;
	Project project;
	GenerateServerCodeExtension extension;

	@Mock
	GenerateServerCodeTask task;

	@BeforeEach
	void setup() {
		projectDir = new File(".");
		project = ProjectBuilder.builder().withName(PROJECT_NAME).withProjectDir(projectDir).build();
		task = spy(project.getTasks().register("task", GenerateServerCodeTask.class).get());
		extension = new GenerateServerCodeExtension(project);
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
		assertEquals(GenerateServerCodeConfiguration.DEFAULT_GENERATE_BATCH_LOADER_ENVIRONMENT.equals("true"),
				task.isGenerateBatchLoaderEnvironment());
		assertEquals(GenerateServerCodeConfiguration.DEFAULT_GENERATE_JPA_ANNOTATION.equals("true"),
				task.isGenerateJPAAnnotation());
		assertEquals(GenerateServerCodeConfiguration.DEFAULT_JAVA_TYPE_FOR_ID_TYPE, task.getJavaTypeForIDType());
		assertEquals(GenerateServerCodeConfiguration.DEFAULT_SCAN_BASE_PACKAGES, task.getScanBasePackages());
		assertEquals(null, task.getSchemaPersonalizationFile());
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
		extension.setGenerateBatchLoaderEnvironment(
				!GenerateServerCodeConfiguration.DEFAULT_GENERATE_BATCH_LOADER_ENVIRONMENT.equals("true"));
		extension.setGenerateJPAAnnotation(
				!GenerateServerCodeConfiguration.DEFAULT_GENERATE_JPA_ANNOTATION.equals("true"));
		extension.setJavaTypeForIDType("MyType");
		extension.setScanBasePackages("my.package");
		extension.setSchemaPersonalizationFile("/my/perso/file.json");

		// Go, go, go

		// Verification
		assertEquals(!GenerateServerCodeConfiguration.DEFAULT_GENERATE_BATCH_LOADER_ENVIRONMENT.equals("true"),
				task.isGenerateBatchLoaderEnvironment());
		assertEquals(!GenerateServerCodeConfiguration.DEFAULT_GENERATE_JPA_ANNOTATION.equals("true"),
				task.isGenerateJPAAnnotation());
		assertEquals("MyType", task.getJavaTypeForIDType());
		assertEquals("my.package", task.getScanBasePackages());
		assertEquals(new File(projectDir, "/my/perso/file.json").getCanonicalPath(),
				task.getSchemaPersonalizationFile().getCanonicalPath());
	}

	/**
	 * Tests that the task values properly override the extension values it they are set
	 * 
	 * @throws IOException
	 */
	@Test
	void test_withTaskValues() throws IOException {
		// Preparation
		task.setGenerateBatchLoaderEnvironment(
				!GenerateServerCodeConfiguration.DEFAULT_GENERATE_BATCH_LOADER_ENVIRONMENT.equals("true"));
		task.setGenerateJPAAnnotation(!GenerateServerCodeConfiguration.DEFAULT_GENERATE_JPA_ANNOTATION.equals("true"));
		task.setJavaTypeForIDType("MyType");
		task.setScanBasePackages("my.package");
		task.setSchemaPersonalizationFile("/my/perso/file.json");

		// Go, go, go

		// Verification
		assertEquals(!GenerateServerCodeConfiguration.DEFAULT_GENERATE_BATCH_LOADER_ENVIRONMENT.equals("true"),
				task.isGenerateBatchLoaderEnvironment());
		assertEquals(!GenerateServerCodeConfiguration.DEFAULT_GENERATE_JPA_ANNOTATION.equals("true"),
				task.isGenerateJPAAnnotation());
		assertEquals("MyType", task.getJavaTypeForIDType());
		assertEquals("my.package", task.getScanBasePackages());
		assertEquals(new File(projectDir, "/my/perso/file.json").getCanonicalPath(),
				task.getSchemaPersonalizationFile().getCanonicalPath());
	}

}
