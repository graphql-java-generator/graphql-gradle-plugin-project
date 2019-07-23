/**
 * 
 */
package graphql.gradleplugin;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.gradle.internal.impldep.org.apache.maven.plugin.MojoExecutionException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.graphql_java_generator.plugin.ResourceSchemaStringProvider;

import graphql.language.Document;
import graphql.parser.Parser;

/**
 * @author EtienneSF
 */
@Configuration
// @Import({ JacksonAutoConfiguration.class })
@ComponentScan(basePackages = { "com.graphql_java_generator" })
public class SpringConfiguration {

	/**
	 * Loads the schema from the graphqls files. This method uses the {@link GraphQLJavaToolsAutoConfiguration} from the
	 * 
	 * project, to load the schema from the graphqls files
	 * 
	 * @throws MojoExecutionException
	 *             When an error occurs while reading or parsing the graphql definition files
	 */
	@Bean
	public List<Document> documents(ResourceSchemaStringProvider schemaStringProvider) throws MojoExecutionException {
		try {
			Parser parser = new Parser();
			return schemaStringProvider.schemaStrings().stream().map(parser::parseDocument)
					.collect(Collectors.toList());
		} catch (IOException e) {
			throw new MojoExecutionException("Error while reading graphql schema definition files: " + e.getMessage(),
					e);
		}

	}
}
