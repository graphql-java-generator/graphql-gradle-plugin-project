package org.example.greeting;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

public class Greeting2 extends DefaultTask {
	private String message;
	private String recipient;

	@Input
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Input
	public String getRecipient() {
		return recipient;
	}

	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}

	@TaskAction
	void sayGreeting() {
		System.out.printf("%s, %s!\n", getMessage(), getRecipient());
	}
}
