package com.graphql_java_generator.gradleplugin;

import java.io.Serializable;

import org.gradle.api.Project;

import com.graphql_java_generator.plugin.conf.GeneratePojoConfiguration;
import com.graphql_java_generator.plugin.conf.PluginMode;

/**
 * Parameters for the GraphQL Gradle plugin.
 * 
 * @author EtienneSF
 *
 */
public class GeneratePojoExtension extends GraphQLExtension implements GeneratePojoConfiguration, Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * <P>
	 * The <A HREF="https://github.com/FasterXML/jackson">Jackson</A> annotations are necessary to properly deserialize
	 * the json, that is incoming from the GraphQL Server. Setting this property to false allows to not generate them.
	 * </P>
	 * <P>
	 * If this property is set to true, the Jackson annotations are added in the generated GraphQL objects. The
	 * <A HREF="https://github.com/FasterXML/jackson">Jackson</A> dependencies must then be added to the target project,
	 * so that the project compiles.
	 * </P>
	 * <P>
	 * The default value is:
	 * </P>
	 * <UL>
	 * <LI><I>true</I> when in <I>client</I> mode.</LI>
	 * <LI><I>false</I> when in <I>server</I> mode.</LI>
	 * </UL>
	 */
	Boolean generateJacksonAnnotations = null;

	public GeneratePojoExtension(Project project) {
		super(project);
	}

	@Override
	public boolean isGenerateJacksonAnnotations() {
		if (generateJacksonAnnotations != null)
			return generateJacksonAnnotations;
		else
			return getMode().equals(PluginMode.client);
	}

	public Boolean isGenerateJacksonAnnotations_Raw() {
		return generateJacksonAnnotations;
	}

	public void setGenerateJacksonAnnotations(boolean generateJacksonAnnotations) {
		this.generateJacksonAnnotations = generateJacksonAnnotations;
		// This task as being configured. So we'll mark compileJava and processResources as depending on it
		setInitialized(true);
	}
}