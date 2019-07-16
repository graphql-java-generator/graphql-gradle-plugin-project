/**
 * 
 */
package graphql.gradleplugin;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

/**
 * Generates the code from the given GraphQL schema.
 * 
 * @author EtienneSF
 */
public class GraphqlGenerateCodeTask extends DefaultTask {

	/** The Gradle extension, to read the plugin parameters from the script */
	private GraphqlExtension extension = null;

	@Input
	public String getPackageName() {
		return getGraphqlExtension().getPackageName();
	}

	@TaskAction
	public void execute() {
		System.out.println("Package name is (from extension) " + getGraphqlExtension().getPackageName());
	}

	public GraphqlExtension getGraphqlExtension() {
		if (extension == null) {
			extension = getProject().getExtensions().findByType(GraphqlExtension.class);
		}
		return extension;
	}

}
