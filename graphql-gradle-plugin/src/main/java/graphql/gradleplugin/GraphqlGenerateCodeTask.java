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

	private String packageName = "com.generated.graphql";

	@Input
	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	@TaskAction
	public void execute() {
		System.out.println("Package name is " + packageName);
	}

}
