package com.graphql_java_generator.gradle_task

import java.util.concurrent.ConcurrentHashMap

import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction

/**
 * This Gradle build service allows to store the process created by the StartApp task, so that the StopApp can stop it. Using 
 * build service is mandatory when using Gradle configuration cache. Otherwise, it's easier to store that StartApp task in the StopApp task. 
 */
abstract class ProcessesService implements BuildService<BuildServiceParameters.None> {
	
	/** 
	 * The processes map for all the processes created by StartApp tasks: 
	 * - Key: the StartApp task path
	 * - Process: the process started by this StartApp task
	 */
	@Internal
	Map<String, Process> processes;
	
	def ProcessesService() {
		processes = new ConcurrentHashMap<>()
	}

	def putProcess(String name, Process process) {
		processes.put(name, process)
	}

	def getProcess(String path) {
		return processes.get(path)
	}
}