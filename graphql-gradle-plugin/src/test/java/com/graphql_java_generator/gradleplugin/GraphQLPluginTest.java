package com.graphql_java_generator.gradleplugin;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GraphQLPluginTest {

	GraphQLPlugin graphQLPlugin;

	@BeforeEach
	void setup() {
		graphQLPlugin = new GraphQLPlugin();
	}

	@Test
	void test_getVersion() {
		String version = graphQLPlugin.getVersion();
		assertNotNull(version);
		assertNotEquals("${projectVersion}", version, "version is: '" + version + "'");
	}

	@Test
	void test_getDependenciesVersion() {
		String version = graphQLPlugin.getDependenciesVersion();
		assertNotNull(version);
		assertNotEquals("${graphqlMavenPluginLogicVersion}", version, "version is: '" + version + "'");
	}

}
