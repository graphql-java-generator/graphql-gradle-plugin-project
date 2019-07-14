package com.company.gradle.binaryrepo;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

public class LatestArtifactVersion extends DefaultTask {
	private String coordinates;
	private String serverUrl;

	@Input
	public String getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(String coordinates) {
		this.coordinates = coordinates;
	}

	@Input
	public String getServerUrl() {
		return serverUrl;
	}

	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

	@TaskAction
	public void resolveLatestVersion() {
		System.out.println("Retrieving artifact " + coordinates + " from " + serverUrl);
		// issue HTTP call and parse response
	}
}
