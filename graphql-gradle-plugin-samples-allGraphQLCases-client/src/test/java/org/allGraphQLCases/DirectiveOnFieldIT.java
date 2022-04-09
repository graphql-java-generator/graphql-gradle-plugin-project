package org.allGraphQLCases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.allGraphQLCases.client.Character;
import org.allGraphQLCases.client.MyQueryType;
import org.allGraphQLCases.client.util.MyQueryTypeExecutorAllGraphQLCases;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.graphql_java_generator.exception.GraphQLRequestExecutionException;
import com.graphql_java_generator.exception.GraphQLRequestPreparationException;

//Adding "webEnvironment = SpringBootTest.WebEnvironment.NONE" avoid this error:
//"No qualifying bean of type 'ReactiveClientRegistrationRepository' available"
//More details here: https://stackoverflow.com/questions/62558552/error-when-using-enablewebfluxsecurity-in-springboot
@SpringBootTest(classes = SpringTestConfig.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Execution(ExecutionMode.CONCURRENT)
class DirectiveOnFieldIT {

	@Autowired
	MyQueryTypeExecutorAllGraphQLCases queryType;

	@Execution(ExecutionMode.CONCURRENT)
	@Test
	void withDirectiveOneParameter() throws GraphQLRequestExecutionException, GraphQLRequestPreparationException {

		// Go, go, go
		MyQueryType resp = queryType.exec(
				"{directiveOnField {id name @testDirective(value: &value) @anotherTestDirective}}", //
				"value", "this is a value");

		// Verifications
		assertNotNull(resp);
		Character ret = resp.getDirectiveOnField();
		assertNotNull(ret);
		assertEquals("this is a value", ret.getName());
	}

	@Execution(ExecutionMode.CONCURRENT)
	@Test
	void testsIssue35() throws GraphQLRequestExecutionException, GraphQLRequestPreparationException {

		// Go, go, go
		MyQueryType resp = queryType.exec(
				"{directiveOnField {id name @testDirective(value: &value)  @anotherTestDirective}}", //
				"value", "this is a value");

		// Verifications
		assertNotNull(resp);
		Character ret = resp.getDirectiveOnField();
		assertNotNull(ret);
		assertEquals("this is a value", ret.getName());
	}

}
