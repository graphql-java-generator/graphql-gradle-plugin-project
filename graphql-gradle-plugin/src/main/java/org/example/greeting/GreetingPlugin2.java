
package org.example.greeting;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class GreetingPlugin2 implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getTasks().create("hello2", Greeting2.class, (task) -> {
			task.setMessage("Hello (2) ");
			task.setRecipient("World");
		});
	}
}
