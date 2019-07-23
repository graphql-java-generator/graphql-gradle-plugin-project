/**
 * 
 */
package graphql.gradleplugin;

import org.gradle.api.Project;

import com.graphql_java_generator.plugin.Logger;

/**
 * @author EtienneSF
 *
 */
public class GradleLogger implements Logger {

	Project project;

	GradleLogger(Project project) {
		this.project = project;
	}

	@Override
	public void debug(String msg) {
		project.getLogger().debug(msg);
	}

	@Override
	public void debug(String msg, Throwable t) {
		project.getLogger().debug(msg, t);
	}

	@Override
	public void error(String msg) {
		project.getLogger().error(msg);
	}

	@Override
	public void error(String msg, Throwable t) {
		project.getLogger().error(msg, t);
	}

	@Override
	public void info(String msg) {
		project.getLogger().info(msg);
	}

	@Override
	public void info(String msg, Throwable t) {
		project.getLogger().info(msg, t);
	}

	@Override
	public boolean isDebugEnabled() {
		return project.getLogger().isDebugEnabled();
	}

	@Override
	public boolean isErrorEnabled() {
		return project.getLogger().isErrorEnabled();
	}

	@Override
	public boolean isInfoEnabled() {
		return project.getLogger().isInfoEnabled();
	}

	@Override
	public boolean isWarnEnabled() {
		return project.getLogger().isWarnEnabled();
	}

	@Override
	public void warn(String msg) {
		project.getLogger().warn(msg);
	}

	@Override
	public void warn(String msg, Throwable t) {
		project.getLogger().warn(msg, t);
	}
}
