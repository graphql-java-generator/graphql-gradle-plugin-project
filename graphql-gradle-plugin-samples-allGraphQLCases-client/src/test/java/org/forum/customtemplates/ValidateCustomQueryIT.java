package org.forum.customtemplates;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.allGraphQLCases.SpringTestConfig;
import org.forum.client.QueryExecutorForum;
import org.forum.client.Subscription;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.graphql_java_generator.exception.GraphQLRequestExecutionException;
import com.graphql_java_generator.exception.GraphQLRequestPreparationException;

@SpringBootTest(classes = SpringTestConfig.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Execution(ExecutionMode.CONCURRENT)
class ValidateCustomQueryIT {

	@Autowired
	QueryExecutorForum query;

	@Test
	void test_customTemplateInTheProject() throws GraphQLRequestPreparationException, GraphQLRequestExecutionException {
		// Let's check that our QueryType is generated from the custom template
		assertTrue(new Subscription().thisIsADummyFieldToCheckThatThisTemplateIsUsed);
	}

	// This custom template is commented: custom templates embedded in an external library doesn't seem to be readable
	// from a Gradle plugin
	// @Test
	// void test_customTemplateInAnExternalJar()
	// throws GraphQLRequestPreparationException, GraphQLRequestExecutionException {
	// // Let's check that our QueryType is generated from the custom template
	// assertTrue(new Query().thisIsADummyFieldToCheckThatThisTemplateIsUsed);
	//
	// // And that it still works! :)
	// List<Board> response = query.boards("{id name}");
	// assertNotNull(response);
	// assertTrue(response.size() > 0);
	// assertTrue(response.get(0) instanceof Board);
	// }

}
