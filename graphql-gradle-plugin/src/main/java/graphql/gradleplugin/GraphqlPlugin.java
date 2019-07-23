/**
 * 
 */
package graphql.gradleplugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * @author EtienneSF
 *
 */
public class GraphqlPlugin implements Plugin<Project> {

	/** The extension name to configure the GraphQL plugin */
	final public static String GRAPHQL_EXTENSION = "graphql";
	/** The name of the task that generates the code from the given GraphQL schemas */
	final public static String GRAPHQL_GENERATE_CODE_TASK_NAME = "graphqlGenerateCode";

	@Override
	public void apply(Project project) {
		project.getExtensions().create(GRAPHQL_EXTENSION, GraphqlExtension.class, project);
		project.getTasks().register(GRAPHQL_GENERATE_CODE_TASK_NAME, GraphqlGenerateCodeTask.class, project);
	}

}
