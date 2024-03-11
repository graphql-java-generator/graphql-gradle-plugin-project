/**
 * 
 */
package com.graphql_java_generator.gradleplugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.language.jvm.tasks.ProcessResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.graphql_java_generator.plugin.conf.Packaging;

/**
 * @author EtienneSF
 *
 */
public class GraphQLPlugin implements Plugin<Project> {

	private static final Logger logger = LoggerFactory.getLogger(GraphQLPlugin.class);

	/** The extension name to configure the GraphQL plugin, for the client code generation task */
	final public static String GENERATE_CLIENT_CODE_EXTENSION = "generateClientCodeConf";
	/** The name of the task that generates the client code from the given GraphQL schemas */
	final public static String GENERATE_CLIENT_CODE_TASK_NAME = "generateClientCode";

	/** The extension name to configure the GraphQL plugin, for the generatePojo task */
	final public static String GENERATE_POJO_EXTENSION = "generatePojoConf";
	/** The name of the task that generates the POJOs from the given GraphQL schemas */
	final public static String GENERATE_POJO_TASK_NAME = "generatePojo";

	/** The extension name to configure the GraphQL plugin, for the server code generation task */
	final public static String GENERATE_SERVER_CODE_EXTENSION = "generateServerCodeConf";
	/** The name of the task that generates the server code from the given GraphQL schemas */
	final public static String GENERATE_SERVER_CODE_TASK_NAME = "generateServerCode";

	/** The extension name to configure the GraphQL plugin, for the code generation task */
	final public static String GRAPHQL_EXTENSION = "graphql";
	/** The name of the task that generates the code from the given GraphQL schemas */
	final public static String GRAPHQL_GENERATE_CODE_TASK_NAME = "graphqlGenerateCode";

	/** The extension name to configure the GraphQL merge task */
	final public static String MERGE_EXTENSION = "generateGraphQLSchemaConf";
	/** The name of the task that generates a GraphqL that merges several GraphQL schemas */
	final public static String MERGE_TASK_NAME = "generateGraphQLSchema";

	/**
	 * The properties loaded from application.properties
	 * 
	 * @see #getProperties()
	 */
	private Properties properties = null;

	@Override
	public void apply(Project project) {
		applyGenerateClientCode(project);
		applyGeneratePojo(project);
		applyGenerateServerCode(project);
		applyGraphQLGenerateCode(project);
		applyGenerateGraphQLSchema(project);

		// The generated resources must be declared:
		// - Not here as it's too soon: the project has not been evaluated yet
		// - Not in the Task.execute method, as this method is not executed if the task is up-to-date
		// So we create a dedicated Action for that. Let's register it, so that it is executed once the project is
		// evaluated.
		project.afterEvaluate(new Action<Project>() {

			@Override
			public void execute(Project p) {
				Set<Task> compileJavaTasks = project.getTasksByName("compileJava", false);
				Set<Task> processResourcesTasks = project.getTasksByName("processResources", false);
				Set<Task> allDependingTasks = new HashSet<>();
				allDependingTasks.addAll(compileJavaTasks);
				allDependingTasks.addAll(processResourcesTasks);

				Set<String> list = new TreeSet<>();
				for (Task t : p.getTasks()) {
					if (t.hasProperty("initialized")) {
						list.add(String.format(
								"[in project.afterEvaluate (getTasks)]   %1s %2s (initialized=[%3s]%4s, enabled=%5s, extensions=%6s)",
								p.getName(), t.getPath(), t.property("initialized").getClass().getSimpleName(),
								t.property("initialized"), t.getEnabled(), t.getExtensions()));
						if ((boolean) t.property("initialized") && t instanceof CommonTask) {
							configurePluginTasks(p, t, allDependingTasks);
						} else {
							logger.debug(
									"Task {} ignored, as its initialized state is {} and it is an instance of {} (it will not be added as a dependency for the compileJava and processResources tasks)",
									t.getName(), t.property("initialized"), t.getClass().getName());
						}
					}
				}
				for (String s : list) {
					logger.debug(s);
				}
			}

			/**
			 * Add a dependency on taskName, like this: <I>taskName.dependsOn(dependsOnTask)</I>.
			 * 
			 * @param taskName
			 * @param dependsOnTask
			 * @return the set of tasks, that has <I>taskName<I> as a name. This set contains at least one item (and
			 *         should not contain more than one)
			 */
			private void configurePluginTasks(Project p, Task task, Set<Task> dependingTasks) {

				// Step1 : the plugin task is marked as a dependency for standard tasks
				for (Task dependingTask : dependingTasks) {
					logger.info("Adding the {} task as a dependency for the {} task", task.getPath(),
							dependingTask.getPath());
					dependingTask.dependsOn(task);
				}

				// Step 2: Add the generated source and resource folders, if applicable
				if (task instanceof GenerateCodeCommonTask) {
					GenerateCodeCommonTask t = (GenerateCodeCommonTask) task;
					// Let's add the folders where the sources and resources have been generated to the project
					SourceSet main = ((SourceSetContainer) project.getProperties().get("sourceSets"))
							.getByName(SourceSet.MAIN_SOURCE_SET_NAME);
					main.getJava().srcDir(t.getTargetSourceFolder());

					addGeneratedResourceFolder(project, task, t.getTargetResourceFolder());
				}

				// Step 3 :
				if (task instanceof GenerateGraphQLSchemaTask) {
					addGeneratedResourceFolder(project, task, ((GenerateGraphQLSchemaTask) task).getTargetFolder());
				}
			}
		});

	}

	/**
	 * Retrieve the gradle project.version, that has been copied into the 'plugin.version' property of the
	 * 'application.properties' file
	 */
	public String getVersion() {
		return getProperties().get("plugin.version").toString();
	}

	/**
	 * Retrieve the version of the <i>graphql-maven-plugin-logic</i> library, that contains all the plugin logic. This
	 * version has been copied into the 'plugin.version' property of the 'application.properties' file
	 */
	public String getDependenciesVersion() {
		return getProperties().get("graphqlMavenPluginLogic.version").toString();
	}

	/** Manual reading of the application.properties file, as this is not a spring boot project. */
	private Properties getProperties() {
		if (this.properties == null) {
			this.properties = new Properties();
			try {
				try (InputStream is = getClass().getResourceAsStream("/application.properties")) {
					this.properties.load(is);
				}
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage(), e);
			} // try
		} // if
		return this.properties;
	}// getProperties()

	/**
	 * Applies the <I>generateClientCode</I> task
	 * 
	 * @param project
	 */
	private void applyGenerateClientCode(Project project) {
		GenerateClientCodeExtension extension = project.getExtensions().create(GENERATE_CLIENT_CODE_EXTENSION,
				GenerateClientCodeExtension.class, project.getProjectDir());

		logger.debug("Applying generateClientCode task");
		GenerateClientCodeTask task = project.getTasks().create(GENERATE_CLIENT_CODE_TASK_NAME,
				GenerateClientCodeTask.class);
		task.setExtension(extension);

		// Apply the java plugin, then add the generated source
		project.getPlugins().apply(JavaPlugin.class);
	}

	/**
	 * Applies the <I>generateGraphQLSchema</I> task
	 * 
	 * @param project
	 */
	private void applyGenerateGraphQLSchema(Project project) {
		GenerateGraphQLSchemaExtension extension = project.getExtensions().create(MERGE_EXTENSION,
				GenerateGraphQLSchemaExtension.class, project.getProjectDir());
		logger.debug("Applying generateGraphQLSchema task");
		GenerateGraphQLSchemaTask task = project.getTasks().create(MERGE_TASK_NAME, GenerateGraphQLSchemaTask.class);
		task.setExtension(extension);
	}

	/**
	 * Applies the <I>generatePojo</I> task
	 * 
	 * @param project
	 */
	private void applyGeneratePojo(Project project) {
		GeneratePojoExtension extension = project.getExtensions().create(GENERATE_POJO_EXTENSION,
				GeneratePojoExtension.class, project.getProjectDir());
		logger.debug("Applying generatePojo task");
		GeneratePojoTask task = project.getTasks().create(GENERATE_POJO_TASK_NAME, GeneratePojoTask.class);
		task.setExtension(extension);

		// Apply the java plugin, then add the generated source
		project.getPlugins().apply(JavaPlugin.class);
	}

	/**
	 * Applies the <I>generateServerCode</I> task
	 * 
	 * @param project
	 * @return
	 */
	private void applyGenerateServerCode(Project project) {
		Packaging packaging = (project.getTasksByName("war", false).size() >= 1) ? Packaging.war : Packaging.jar;

		GenerateServerCodeExtension extension = project.getExtensions().create(GENERATE_SERVER_CODE_EXTENSION,
				GenerateServerCodeExtension.class, project.getProjectDir(), packaging);
		logger.info("Applying generateServerCode task");
		GenerateServerCodeTask task = project.getTasks().create(GENERATE_SERVER_CODE_TASK_NAME,
				GenerateServerCodeTask.class);
		task.setExtension(extension);

		// Apply the java plugin, then add the generated source
		project.getPlugins().apply(JavaPlugin.class);
	}

	/**
	 * Applies the <I>graphqlGenerateCode</I> task
	 * 
	 * @param project
	 */
	private void applyGraphQLGenerateCode(Project project) {
		Packaging packaging = (project.getTasksByName("war", false).size() >= 1) ? Packaging.war : Packaging.jar;

		GraphQLExtension extension = project.getExtensions().create(GRAPHQL_EXTENSION, GraphQLExtension.class,
				project.getProjectDir(), packaging);
		logger.debug("Applying GraphQL task");
		GraphQLGenerateCodeTask task = project.getTasks().create(GRAPHQL_GENERATE_CODE_TASK_NAME,
				GraphQLGenerateCodeTask.class);
		task.setExtension(extension);

		// Apply the java plugin, then add the generated source
		project.getPlugins().apply(JavaPlugin.class);

	}

	/** Add the given resource folder to the resource folders list, if it wasn't already added. */
	private void addGeneratedResourceFolder(Project project, Task task, File newResourcFolder) {
		SourceSet main = ((SourceSetContainer) project.getProperties().get("sourceSets"))
				.getByName(SourceSet.MAIN_SOURCE_SET_NAME);
		String newResourceFolder = newResourcFolder.getAbsolutePath();
		java.util.Optional<File> existingResourceFolder = main.getResources().getFiles().stream()
				.filter(f -> f.getAbsolutePath().equals(newResourceFolder)).findFirst();
		if (!existingResourceFolder.isPresent()) {
			logger.info("Adding '" + newResourcFolder + "' folder to the resources folders list for task '"
					+ task.getName() + "'");
			main.getResources().srcDir(newResourcFolder);
		} else {
			logger.debug("Ignoring '" + newResourcFolder + "' resource folder for task '" + task.getName()
					+ "', as it is already listed");
		}

		if (logger.isInfoEnabled()) {
			List<String> paths = new ArrayList<>();
			for (File f : main.getResources().getSrcDirs()) {
				paths.add(f.getAbsolutePath());
			}
			logger.info("Resources folders are: [" + String.join(",", paths) + "]");
		}

		// Due to a Gradle 7 bug, that is qualified as "Won't be fixed", we need to force a duplicatesStrategy for the
		// processResources task.
		// More info here: https://github.com/gradle/gradle/issues/17236
		//
		// This is ugly. But the Gradle team doesn't care about this issue... :(
		project.getTasksByName("processResources", false)
				.forEach(t -> ((ProcessResources) t).setDuplicatesStrategy(DuplicatesStrategy.EXCLUDE));
	}
}
