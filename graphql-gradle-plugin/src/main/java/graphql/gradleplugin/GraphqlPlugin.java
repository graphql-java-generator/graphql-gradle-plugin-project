/**
 * 
 */
package graphql.gradleplugin;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * @author EtienneSF
 *
 */
public class GraphqlPlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		// project.getExtensions().create("graphql", GraphqlExtension.class, project);

		project.getTasks().register("graphqlGenerateCode", GraphqlGenerateCodeTask.class,
				new Action<GraphqlGenerateCodeTask>() {
					@Override
					public void execute(GraphqlGenerateCodeTask task) {
						// No action
					}
				});
	}

}
