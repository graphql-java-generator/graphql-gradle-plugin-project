package graphql.gradleplugin;

import org.gradle.api.Project;

/**
 * Parameters for the GraphQL Gradle plugin.
 * 
 * @author EtienneSF
 *
 */
public class GraphqlExtension {

	final Project project;

	private String packageName = "com.generated.graphql";

	public GraphqlExtension(Project project) {
		this.project = project;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

}
