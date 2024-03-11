package com.graphql_java_generator.gradle_task

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction

/**
 * The class below is largely inspired from Peter Ledbrook's answer in this stackoverflow thread:
 * https://stackoverflow.com/questions/31407323/running-integration-tests-for-a-spring-boot-rest-service-using-gradle
 */
public abstract class StartApp extends DefaultTask {

	static enum Status {
		UP, DOWN, TIMED_OUT
	}

	@InputFile
	File jarFile

	@Input
	String url

	/** The process that will be created by this task */
	@Internal
	Process process

	/** The Gradle build service that contains the reference for all processes created by StartApp tasks */
	@ServiceReference("startApp")
	abstract Property<ProcessesService> getProcessesServices()


	@TaskAction
	def startApp() {
		logger.info "Starting server"
		logger.info "Application jar file: " + jarFile

		def args = ["java", "-jar", jarFile.path]
		def pb = new ProcessBuilder(args)
		pb.redirectErrorStream(true)
		process = pb.start()
		getProcessesServices().get().putProcess(path, process)

		// The started process must be killed if the JVM exits.
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
					public void run() {
						process.destroy()
					}
				}))


		final output = new StringBuffer()
		process.consumeProcessOutputStream(output)

		def status = Status.TIMED_OUT
		for (i in 0..20) {
			Thread.sleep(3000)

			if (hasServerExited(process)) {
				throw new RuntimeException("Server exited")
			}

			if ( (status=getStatus(url)) == 200) {
				// Ok, we're done
				break
			}
		}

		// Let's check we're done
		if (status != 200) {
			process.destroy()
			throw new RuntimeException("Server failed to start up. Status: ${status}")
		}
	}

	def stopApp(){
		// Let's kill the process
		process.destroy()
	}

	int getStatus(String url) throws IOException {
		try {
			logger.info "Checking server on '${url}'"
			URL siteURL = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) siteURL.openConnection();
			connection.setRequestMethod("GET");
			connection.setConnectTimeout(3000);
			connection.connect();

			return connection.getResponseCode();
		} catch (Exception e) {
			return -1
		}
	}

	protected boolean hasServerExited(Process process) {
		try {
			process.exitValue()
			return true
		} catch (IllegalThreadStateException ex) {
			return false
		}
	}
}