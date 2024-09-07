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
		projectDir = new File(".");
		project = ProjectBuilder.builder().withName(PROJECT_NAME).withProjectDir(projectDir).build();
		extension = new GenerateCodeCommonExtension(project.getLayout());
		task = spy(project.getTasks().register("task", GenerateCodeCommonTask.class, extension).get());
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
		assertEquals(GenerateCodeCommonConfiguration.DEFAULT_COPY_RUNTIME_SOURCES.equals("true"),
				task.isCopyRuntimeSources());
		assertEquals(0, task.getCustomScalars().size());
		assertEquals(GenerateCodeCommonConfiguration.DEFAULT_PACKAGE_NAME, task.getPackageName());
		assertEquals(GenerateCodeCommonConfiguration.DEFAULT_SEPARATE_UTIL_CLASSES.equals("true"),
				task.isSeparateUtilityClasses());
		assertEquals(GenerateCodeCommonConfiguration.DEFAULT_SOURCE_ENCODING, task.getSourceEncoding());
		assertEquals(GenerateCodeCommonConfiguration.DEFAULT_SPRING_BEAN_SUFFIX, task.getSpringBeanSuffix());
		assertEquals(new File(projectDir, "./build/generated/resources/graphqlGradlePlugin").getCanonicalPath(),
				task.getTargetResourceFolder().getCanonicalPath());
		assertEquals(new File(projectDir, "./build/generated/sources/graphqlGradlePlugin").getCanonicalPath(),
				task.getTargetSourceFolder().getCanonicalPath());
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
		extension.setCopyRuntimeSources(!GenerateCodeCommonConfiguration.DEFAULT_COPY_RUNTIME_SOURCES.equals("true"));
		CustomScalarDefinition[] scalars = new CustomScalarDefinition[0];
		extension.setCustomScalars(scalars);
		extension.setPackageName("a.package");
		extension.setSeparateUtilityClasses(
				!GenerateCodeCommonConfiguration.DEFAULT_SEPARATE_UTIL_CLASSES.equals("true"));
		extension.setSourceEncoding("UTF 666");
		extension.setSpringBeanSuffix("suffix");
		extension.setTargetResourceFolder("resourcesFolder");
		extension.setTargetSourceFolder("sourcesFolder");

		// Go, go, go

		// Verification
		assertEquals(!GenerateCodeCommonConfiguration.DEFAULT_COPY_RUNTIME_SOURCES.equals("true"),
				task.isCopyRuntimeSources());
		// assertEquals(scalars, task.getCustomScalars());
		assertEquals("a.package", task.getPackageName());
		assertEquals(!GenerateCodeCommonConfiguration.DEFAULT_SEPARATE_UTIL_CLASSES.equals("true"),
				task.isSeparateUtilityClasses());
		assertEquals("UTF 666", task.getSourceEncoding());
		assertEquals("suffix", task.getSpringBeanSuffix());
		assertEquals(new File(projectDir, "resourcesFolder").getCanonicalPath(),
				task.getTargetResourceFolder().getCanonicalPath());
		assertEquals(new File(projectDir, "sourcesFolder").getCanonicalPath(),
				task.getTargetSourceFolder().getCanonicalPath());
	}

	/**
	 * Tests that the task values properly override the extension values it they are set
	 * 
	 * @throws IOException
	 */
	@Test
	void test_withTaskValues() throws IOException {
		// Preparation
		task.setCopyRuntimeSources(!GenerateCodeCommonConfiguration.DEFAULT_COPY_RUNTIME_SOURCES.equals("true"));
		CustomScalarDefinition[] scalars = new CustomScalarDefinition[0];
		task.setCustomScalars(scalars);
		task.setPackageName("a.package");
		task.setSeparateUtilityClasses(!GenerateCodeCommonConfiguration.DEFAULT_SEPARATE_UTIL_CLASSES.equals("true"));
		task.setSourceEncoding("UTF 666");
		task.setSpringBeanSuffix("suffix");
		task.setTargetResourceFolder("resourcesFolder");
		task.setTargetSourceFolder("sourcesFolder");

		// Go, go, go

		// Verification
		assertEquals(!GenerateCodeCommonConfiguration.DEFAULT_COPY_RUNTIME_SOURCES.equals("true"),
				task.isCopyRuntimeSources());
		// assertEquals(scalars, task.getCustomScalars());
		assertEquals("a.package", task.getPackageName());
		assertEquals(!GenerateCodeCommonConfiguration.DEFAULT_SEPARATE_UTIL_CLASSES.equals("true"),
				task.isSeparateUtilityClasses());
		assertEquals("UTF 666", task.getSourceEncoding());
		assertEquals("suffix", task.getSpringBeanSuffix());
		assertEquals(new File(projectDir, "resourcesFolder").getCanonicalPath(),
				task.getTargetResourceFolder().getCanonicalPath());
		assertEquals(new File(projectDir, "sourcesFolder").getCanonicalPath(),
				task.getTargetSourceFolder().getCanonicalPath());
	}

}
