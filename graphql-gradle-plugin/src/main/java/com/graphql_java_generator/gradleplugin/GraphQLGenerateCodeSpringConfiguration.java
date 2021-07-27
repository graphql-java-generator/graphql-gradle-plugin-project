/**
 * 
 */
package com.graphql_java_generator.gradleplugin;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.gradle.api.GradleScriptException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

import com.graphql_java_generator.plugin.DocumentParser;
import com.graphql_java_generator.plugin.ResourceSchemaStringProvider;
import com.graphql_java_generator.util.GraphqlUtils;

import graphql.language.Document;
import graphql.parser.Parser;

/**
 * @author EtienneSF
 */
@Configuration
// @Import({ JacksonAutoConfiguration.class })
@ComponentScan(basePackageClasses = { DocumentParser.class, GraphqlUtils.class }, //
		excludeFilters = { @Filter(type = FilterType.REGEX, pattern = ".*\\.GenerateClientCode.*"),
				@Filter(type = FilterType.REGEX, pattern = ".*\\.GenerateGraphQLSchema.*"),
				@Filter(type = FilterType.REGEX, pattern = ".*\\.GeneratePojo.*"),
				@Filter(type = FilterType.REGEX, pattern = ".*\\.GenerateServerCode.*") })
public class GraphQLGenerateCodeSpringConfiguration {

	/**
	 * The current GraphQL extension, that contains the plugin configuration. It is set by the
	 * {@link GraphQLGenerateCodeTask} task, before it starts the Spring context
	 */
	static GraphQLGenerateCodeTask graphqlGenerateCodeConf = null;

	@Bean
	GraphQLGenerateCodeTask graphqlGenerateCodeConf() {
		return graphqlGenerateCodeConf;
	}

	/**
	 * Loads the schema from the graphqls files. This method uses the GraphQLJavaToolsAutoConfiguration from the
	 * project, to load the schema from the graphqls files
	 * 
	 * @param schemaStringProvider
	 *            The String Provider
	 * @return the {@link Document}s to read
	 * @throws GradleScriptException
	 *             When an error occurs while reading or parsing the graphql definition files
	 */
	@Bean
	public List<Document> documents(ResourceSchemaStringProvider schemaStringProvider) throws GradleScriptException {
		try {
			Parser parser = new Parser();
			return schemaStringProvider.schemaStrings().stream().map(parser::parseDocument)
					.collect(Collectors.toList());
		} catch (IOException e) {
			throw new GradleScriptException("Error while reading graphql schema definition files: " + e.getMessage(),
					e);
		}

	}
}
