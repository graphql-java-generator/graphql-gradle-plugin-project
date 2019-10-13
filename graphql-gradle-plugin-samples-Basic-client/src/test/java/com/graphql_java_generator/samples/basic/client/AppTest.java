package com.graphql_java_generator.samples.basic.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.generated.graphql.Query;
import com.graphql_java_generator.client.request.ObjectResponse;
import com.graphql_java_generator.client.response.GraphQLExecutionException;
import com.graphql_java_generator.client.response.GraphQLRequestPreparationException;

/**
 * Integration test for the Basic Client. It calls the Basic Server, and checks it answer.
 * 
 */
public class AppTest {

	final static String GRAPHQL_SERVER_URL = "http://localhost:8180/graphql";

	// For direct query (the query is prepared for each execution)
	Query query;

	// For prepared queries, the reponse is already built, and reuse for each call.
	ObjectResponse helloResponse;

	@BeforeAll
	void beforeEach() throws GraphQLExecutionException, GraphQLRequestPreparationException {
		query = new Query(GRAPHQL_SERVER_URL);

		helloResponse = query.getHelloResponseBuilder().withQueryResponseDef("").build();
	}

	@Test
	void testServerCall_DirectQuery() throws GraphQLExecutionException, GraphQLRequestPreparationException {
		assertEquals("Hello World", query.hello("", "World"));
	}

	@Test
	void testServerCall_PreparedQuery() throws GraphQLExecutionException, GraphQLRequestPreparationException {
		assertEquals("Hello World", query.hello(helloResponse, "World"));
	}
}
