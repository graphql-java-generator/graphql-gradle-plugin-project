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
import com.graphql_java_generator.plugin.conf.Packaging;

public class GenerateServerCodeTaskTest {

	static final String PROJECT_NAME = "A Dummy project";
	File projectDir;
	Project project;
	GenerateServerCodeExtension extension;

	@Mock
	GenerateServerCodeTask task;

	@BeforeEach
	void setup() {
		this.projectDir = new File(".");
		this.project = ProjectBuilder.builder().withName(PROJECT_NAME).withProjectDir(this.projectDir).build();
		this.extension = new GenerateServerCodeExtension(this.project.getProjectDir(), Packaging.war);
		this.task = spy(this.project.getTasks().register("task", GenerateServerCodeTask.class).get());
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
		assertEquals(GenerateServerCodeConfiguration.DEFAULT_GENERATE_BATCH_LOADER_ENVIRONMENT.equals("true"),
				this.task.isGenerateBatchLoaderEnvironment());
		assertEquals(GenerateServerCodeConfiguration.DEFAULT_GENERATE_JPA_ANNOTATION.equals("true"),
				this.task.isGenerateJPAAnnotation());
		assertEquals(GenerateServerCodeConfiguration.DEFAULT_JAVA_TYPE_FOR_ID_TYPE, this.task.getJavaTypeForIDType());
		assertEquals(GenerateServerCodeConfiguration.DEFAULT_SCAN_BASE_PACKAGES, this.task.getScanBasePackages());
		assertEquals(null, this.task.getSchemaPersonalizationFile());
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
		this.extension.setGenerateBatchLoaderEnvironment(
				!GenerateServerCodeConfiguration.DEFAULT_GENERATE_BATCH_LOADER_ENVIRONMENT.equals("true"));
		this.extension.setGenerateJPAAnnotation(
				!GenerateServerCodeConfiguration.DEFAULT_GENERATE_JPA_ANNOTATION.equals("true"));
		this.extension.setJavaTypeForIDType("MyType");
		this.extension.setScanBasePackages("my.package");
		this.extension.setSchemaPersonalizationFile("/my/perso/file.json");

		// Go, go, go

		// Verification
		assertEquals(!GenerateServerCodeConfiguration.DEFAULT_GENERATE_BATCH_LOADER_ENVIRONMENT.equals("true"),
				this.task.isGenerateBatchLoaderEnvironment());
		assertEquals(!GenerateServerCodeConfiguration.DEFAULT_GENERATE_JPA_ANNOTATION.equals("true"),
				this.task.isGenerateJPAAnnotation());
		assertEquals("MyType", this.task.getJavaTypeForIDType());
		assertEquals("my.package", this.task.getScanBasePackages());
		assertEquals(new File(this.projectDir, "/my/perso/file.json").getCanonicalPath(),
				this.task.getSchemaPersonalizationFile().getCanonicalPath());
	}

	/**
	 * Tests that the task values properly override the extension values it they are set
	 * 
	 * @throws IOException
	 */
	@Test
	void test_withTaskValues() throws IOException {
		// Preparation
		this.task.setGenerateBatchLoaderEnvironment(
				!GenerateServerCodeConfiguration.DEFAULT_GENERATE_BATCH_LOADER_ENVIRONMENT.equals("true"));
		this.task.setGenerateJPAAnnotation(
				!GenerateServerCodeConfiguration.DEFAULT_GENERATE_JPA_ANNOTATION.equals("true"));
		this.task.setJavaTypeForIDType("MyType");
		this.task.setScanBasePackages("my.package");
		this.task.setSchemaPersonalizationFile("/my/perso/file.json");

		// Go, go, go

		// Verification
		assertEquals(!GenerateServerCodeConfiguration.DEFAULT_GENERATE_BATCH_LOADER_ENVIRONMENT.equals("true"),
				this.task.isGenerateBatchLoaderEnvironment());
		assertEquals(!GenerateServerCodeConfiguration.DEFAULT_GENERATE_JPA_ANNOTATION.equals("true"),
				this.task.isGenerateJPAAnnotation());
		assertEquals("MyType", this.task.getJavaTypeForIDType());
		assertEquals("my.package", this.task.getScanBasePackages());
		assertEquals(new File(this.projectDir, "/my/perso/file.json").getCanonicalPath(),
				this.task.getSchemaPersonalizationFile().getCanonicalPath());
	}

}
