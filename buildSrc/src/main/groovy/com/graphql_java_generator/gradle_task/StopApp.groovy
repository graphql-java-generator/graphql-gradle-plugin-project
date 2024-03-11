package com.graphql_java_generator.gradle_task

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * The class below is largely inspired from Peter Ledbrook's answer in this stackoverflow thread:
 * https://stackoverflow.com/questions/31407323/running-integration-tests-for-a-spring-boot-rest-service-using-gradle
 */
public abstract class StopApp extends DefaultTask {

	/** The Gradle build service that contains the reference for all processes created by StartApp tasks */
	@ServiceReference("startApp")
	abstract Property<ProcessesService> getProcessesServices()

	@Input
	String startAppTaskPath

	@TaskAction
	def stopApp(){
		getProcessesServices().get().getProcess(startAppTaskPath).destroy()
	}

}