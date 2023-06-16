/**
 * 
 */
package com.graphql_java_generator.gradleplugin;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

import com.graphql_java_generator.plugin.DocumentParser;
import com.graphql_java_generator.util.GraphqlUtils;

/**
 * @author EtienneSF
 */
@Configuration
@ComponentScan(basePackageClasses = { DocumentParser.class, GraphqlUtils.class }, //
		excludeFilters = { @Filter(type = FilterType.REGEX, pattern = ".*\\.GenerateClientCode.*"),
				@Filter(type = FilterType.REGEX, pattern = ".*\\.GenerateCode.*"),
				@Filter(type = FilterType.REGEX, pattern = ".*\\.GeneratePojo.*"),
				@Filter(type = FilterType.REGEX, pattern = ".*\\.GenerateServerCode.*"),
				@Filter(type = FilterType.REGEX, pattern = ".*\\.GraphQL.*") })
public class GenerateGraphQLSchemaSpringConfiguration {

	/**
	 * The current GraphQL extension, that contains the plugin configuration. It is set by the
	 * {@link GenerateGraphQLSchemaTask} task, before it starts the Spring context
	 */
	static GenerateGraphQLSchemaTask generateGraphQLSchemaConf = null;

	@Bean
	GenerateGraphQLSchemaTask generateGraphQLSchemaConf() {
		return generateGraphQLSchemaConf;
	}

}
