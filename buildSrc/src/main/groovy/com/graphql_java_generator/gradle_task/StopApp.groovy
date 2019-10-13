package com.graphql_java_generator.gradle_task

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction


/**
 * The class below is largely inspired from Peter Ledbrook's answer in this stackoverflow thread:
 * https://stackoverflow.com/questions/31407323/running-integration-tests-for-a-spring-boot-rest-service-using-gradle
 */
class StopApp extends DefaultTask {

	@Input
	Process process

	@TaskAction
	def stopApp(){
		process.destroy()
	}

	//		@Input
	//	String urlPath
	//
	//	@TaskAction
	//	def stopApp(){
	//		def url = new URL(urlPath)
	//		def connection = url.openConnection()
	//		connection.requestMethod = "POST"
	//		connection.doOutput = true
	//		connection.outputStream.close()
	//		connection.inputStream.close()
	//	}
}