/**
 * 
 */
package com.graphql_java_generator.gradleplugin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.commons.io.FileUtils;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.graphql_java_generator.plugin.conf.Packaging;

/**
 * Implementation of a gradle functional test
 * 
 * @author etienne-sf
 */
@Disabled
public class FunctionalTest {

	File testProjectDir;

	final String ROOT = "src/test/resources/functionalTest";

	/**
	 * Create a folder for this test in 'build/functionalTests', remove it if it existed before, and copy their the
	 * settings.gradle, gradle.properties (from the root project) and the graphqls schema.<br/>
	 * The folder is saved into the {@link #testProjectDir} attribute of this class
	 * 
	 * 
	 * @param testName
	 * @throws IOException
	 */
	void setup(String testName) throws IOException {

		this.testProjectDir = new File("build/functionalTests", testName);

		if (this.testProjectDir.exists()) {
			FileUtils.deleteDirectory(this.testProjectDir);
		}
		this.testProjectDir.mkdirs();

		// Creation of the temp space where the test project will run (to avoid polluting the git space)
		new File(this.testProjectDir, "src/main/resources").mkdirs();
		Files.copy(//
				Paths.get(this.ROOT, "settings.gradle"), //
				Paths.get(this.testProjectDir.getAbsolutePath(), "settings.gradle"), //
				StandardCopyOption.REPLACE_EXISTING);
		Files.copy(//
				Paths.get("../gradle.properties"), //
				Paths.get(this.testProjectDir.getAbsolutePath(), "gradle.properties"), //
				StandardCopyOption.REPLACE_EXISTING);
		Files.copy(//
				Paths.get(this.ROOT, "basic.graphqls"), //
				Paths.get(this.testProjectDir.getAbsolutePath(), "src/main/resources/basic.graphqls"), //
				StandardCopyOption.REPLACE_EXISTING);
	}

	@Test
	void testBasicSchema_usingExtension() throws IOException {
		setup("testBasicSchema_usingExtension");
		Files.copy(//
				Paths.get(this.ROOT, "build_usingExtension.gradle"), //
				Paths.get(this.testProjectDir.getAbsolutePath(), "build.gradle"), //
				StandardCopyOption.REPLACE_EXISTING);

		BuildResult result = GradleRunner.create()//
				.withProjectDir(this.testProjectDir)//
				.withArguments("build")//
				.withPluginClasspath()//
				.build();

		result.getOutput().contains("to be implemented");
		assertEquals(TaskOutcome.SUCCESS, result.task(":generateClientCode").getOutcome());
		assertEquals(TaskOutcome.SKIPPED, result.task(":generatePojo").getOutcome());
		assertEquals(Packaging.war, result.task(":generateServerCode").getOutcome());
	}

	@Test
	void testBasicSchema_registerTask() throws IOException {
		setup("testBasicSchema_registerTask");
		Files.copy(//
				Paths.get(this.ROOT, "build_registerTask.gradle"), //
				Paths.get(this.testProjectDir.getAbsolutePath(), "build.gradle"), //
				StandardCopyOption.REPLACE_EXISTING);

		BuildResult result = GradleRunner.create()//
				.withProjectDir(this.testProjectDir)//
				.withArguments("build")//
				.withPluginClasspath()//
				.build();

		result.getOutput().contains("to be implemented");
		assertEquals(TaskOutcome.SUCCESS, result.task(":generateClientCode").getOutcome());
		assertEquals(TaskOutcome.SKIPPED, result.task(":generatePojo").getOutcome());
		assertEquals(Packaging.war, result.task(":generateServerCode").getOutcome());
	}

	@Test
	void testBasicSchema_generateServerCode() throws IOException {
		setup("testBasicSchema_generateServerCode");
		Files.copy(//
				Paths.get(this.ROOT, "build_generateServerCode.gradle"), //
				Paths.get(this.testProjectDir.getAbsolutePath(), "build.gradle"), //
				StandardCopyOption.REPLACE_EXISTING);

		BuildResult result = GradleRunner.create()//
				.withProjectDir(this.testProjectDir)//
				.withArguments("build")//
				.withPluginClasspath()//
				.build();

		result.getOutput().contains("to be implemented");
		assertEquals(TaskOutcome.SUCCESS, result.task(":generateServerCode").getOutcome());
		assertEquals(TaskOutcome.SUCCESS, result.task(":generateServerCodeRegister").getOutcome());

		assertEquals(TaskOutcome.SKIPPED, result.task(":generatePojo").getOutcome());

		assertEquals(Packaging.war, ((GenerateServerCodeTask) result.task(":generateServerCode")).getPackaging());
		assertEquals(Packaging.war,
				((GenerateServerCodeTask) result.task(":generateServerCodeRegister")).getPackaging());
	}

}
