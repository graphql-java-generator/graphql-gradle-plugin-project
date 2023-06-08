package org.forum.customtemplates;

import org.allGraphQLCases.SpringTestConfig;
import org.forum.client.QueryExecutorForum;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = SpringTestConfig.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Execution(ExecutionMode.CONCURRENT)
class ValidateCustomQueryIT {

	@Autowired
	QueryExecutorForum query;
	/*
	 * Using custom templates doesn't work in the gradle plugin version
	 * 
	 * @Test void test_customTemplateInTheProject() throws GraphQLRequestPreparationException,
	 * GraphQLRequestExecutionException { // Let's check that our QueryType is generated from the custom template
	 * assertTrue(new Subscription().thisIsADummyFieldToCheckThatThisTemplateIsUsed); }
	 * 
	 * 
	 * @Test void test_customTemplateInAnExternalJar() throws GraphQLRequestPreparationException,
	 * GraphQLRequestExecutionException { // Let's check that our QueryType is generated from the custom template
	 * assertTrue(new Query().thisIsADummyFieldToCheckThatThisTemplateIsUsed);
	 * 
	 * // And that it still works! :) List<Board> response = query.boards("{id name}"); assertNotNull(response);
	 * assertTrue(response.size() > 0); assertTrue(response.get(0) instanceof Board); }
	 */

}
