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

import com.graphql_java_generator.plugin.conf.CustomScalarDefinition;
import com.graphql_java_generator.plugin.conf.GenerateCodeCommonConfiguration;

public class GenerateCodeCommonTaskTest {

	static final String PROJECT_NAME = "A Dummy project";
	File projectDir;
	Project project;
	GenerateCodeCommonExtension extension;

	@Mock
	GenerateCodeCommonTask task;

	@BeforeEach
	void setup() {
		this.projectDir = new File(".");
		this.project = ProjectBuilder.builder().withName(PROJECT_NAME).withProjectDir(this.projectDir).build();
		this.extension = new GenerateCodeCommonExtension(this.project.getProjectDir());
		this.task = spy(this.project.getTasks().register("task", GenerateCodeCommonTask.class, this.extension).get());
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
		assertEquals(GenerateCodeCommonConfiguration.DEFAULT_COPY_RUNTIME_SOURCES.equals("true"),
				this.task.isCopyRuntimeSources());
		assertEquals(0, this.task.getCustomScalars().size());
		assertEquals(GenerateCodeCommonConfiguration.DEFAULT_PACKAGE_NAME, this.task.getPackageName());
		assertEquals(GenerateCodeCommonConfiguration.DEFAULT_SEPARATE_UTIL_CLASSES.equals("true"),
				this.task.isSeparateUtilityClasses());
		assertEquals(GenerateCodeCommonConfiguration.DEFAULT_SOURCE_ENCODING, this.task.getSourceEncoding());
		assertEquals(GenerateCodeCommonConfiguration.DEFAULT_SPRING_BEAN_SUFFIX, this.task.getSpringBeanSuffix());
		assertEquals(new File(this.projectDir, "./build/generated/resources/graphqlGradlePlugin").getCanonicalPath(),
				this.task.getTargetResourceFolder().getCanonicalPath());
		assertEquals(new File(this.projectDir, "./build/generated/sources/graphqlGradlePlugin").getCanonicalPath(),
				this.task.getTargetSourceFolder().getCanonicalPath());
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
		this.extension
				.setCopyRuntimeSources(!GenerateCodeCommonConfiguration.DEFAULT_COPY_RUNTIME_SOURCES.equals("true"));
		CustomScalarDefinition[] scalars = new CustomScalarDefinition[0];
		this.extension.setCustomScalars(scalars);
		this.extension.setPackageName("a.package");
		this.extension.setSeparateUtilityClasses(
				!GenerateCodeCommonConfiguration.DEFAULT_SEPARATE_UTIL_CLASSES.equals("true"));
		this.extension.setSourceEncoding("UTF 666");
		this.extension.setSpringBeanSuffix("suffix");
		this.extension.setTargetResourceFolder("resourcesFolder");
		this.extension.setTargetSourceFolder("sourcesFolder");

		// Go, go, go

		// Verification
		assertEquals(!GenerateCodeCommonConfiguration.DEFAULT_COPY_RUNTIME_SOURCES.equals("true"),
				this.task.isCopyRuntimeSources());
		// assertEquals(scalars, task.getCustomScalars());
		assertEquals("a.package", this.task.getPackageName());
		assertEquals(!GenerateCodeCommonConfiguration.DEFAULT_SEPARATE_UTIL_CLASSES.equals("true"),
				this.task.isSeparateUtilityClasses());
		assertEquals("UTF 666", this.task.getSourceEncoding());
		assertEquals("suffix", this.task.getSpringBeanSuffix());
		assertEquals(new File(this.projectDir, "resourcesFolder").getCanonicalPath(),
				this.task.getTargetResourceFolder().getCanonicalPath());
		assertEquals(new File(this.projectDir, "sourcesFolder").getCanonicalPath(),
				this.task.getTargetSourceFolder().getCanonicalPath());
	}

	/**
	 * Tests that the task values properly override the extension values it they are set
	 * 
	 * @throws IOException
	 */
	@Test
	void test_withTaskValues() throws IOException {
		// Preparation
		this.task.setCopyRuntimeSources(!GenerateCodeCommonConfiguration.DEFAULT_COPY_RUNTIME_SOURCES.equals("true"));
		CustomScalarDefinition[] scalars = new CustomScalarDefinition[0];
		this.task.setCustomScalars(scalars);
		this.task.setPackageName("a.package");
		this.task.setSeparateUtilityClasses(
				!GenerateCodeCommonConfiguration.DEFAULT_SEPARATE_UTIL_CLASSES.equals("true"));
		this.task.setSourceEncoding("UTF 666");
		this.task.setSpringBeanSuffix("suffix");
		this.task.setTargetResourceFolder("resourcesFolder");
		this.task.setTargetSourceFolder("sourcesFolder");

		// Go, go, go

		// Verification
		assertEquals(!GenerateCodeCommonConfiguration.DEFAULT_COPY_RUNTIME_SOURCES.equals("true"),
				this.task.isCopyRuntimeSources());
		// assertEquals(scalars, task.getCustomScalars());
		assertEquals("a.package", this.task.getPackageName());
		assertEquals(!GenerateCodeCommonConfiguration.DEFAULT_SEPARATE_UTIL_CLASSES.equals("true"),
				this.task.isSeparateUtilityClasses());
		assertEquals("UTF 666", this.task.getSourceEncoding());
		assertEquals("suffix", this.task.getSpringBeanSuffix());
		assertEquals(new File(this.projectDir, "resourcesFolder").getCanonicalPath(),
				this.task.getTargetResourceFolder().getCanonicalPath());
		assertEquals(new File(this.projectDir, "sourcesFolder").getCanonicalPath(),
				this.task.getTargetSourceFolder().getCanonicalPath());
	}

}
