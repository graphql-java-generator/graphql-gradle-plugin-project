/**
 * 
 */
package org.allGraphQLCases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.allGraphQLCases.client.CIP_Character_CIS;
import org.allGraphQLCases.client.CTP_Droid_CTS;
import org.allGraphQLCases.client.CTP_Human_CTS;
import org.allGraphQLCases.client.CTP_MyQueryType_CTS;
import org.allGraphQLCases.client.util.MyQueryTypeExecutorAllGraphQLCases;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.ResponseError;

import com.graphql_java_generator.exception.GraphQLRequestExecutionException;
import com.graphql_java_generator.exception.GraphQLRequestPreparationException;

import graphql.language.SourceLocation;

/**
 * IT test to check Errors management
 * 
 * @author etienne-sf
 */
@SpringBootTest(classes = SpringTestConfig.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Execution(ExecutionMode.CONCURRENT)
public class ErrorIT {

	@Autowired
	MyQueryTypeExecutorAllGraphQLCases myQuery;

	/**
	 * Check the SourceLocation, that is added by the MyInstrumentation class, in the server project
	 * 
	 * @throws GraphQLRequestExecutionException
	 * @throws GraphQLRequestPreparationException
	 */
	@Test
	@Execution(ExecutionMode.CONCURRENT)
	void test_SourceLocation() throws GraphQLRequestExecutionException, GraphQLRequestPreparationException {

		// Go, go, go
		GraphQLRequestExecutionException ex = assertThrows(GraphQLRequestExecutionException.class,
				() -> this.myQuery.exec("{error(errorLabel:\"add a SourceLocation\"){}}"));

		// When the parameter for the error query is "add a SourceLocation", then a special management should be
		// executed on server side. Let's test it.
		assertTrue(ex.getMessage().contains("add a SourceLocation"), "The exception message is: " + ex.getMessage());

		List<ResponseError> errors = ex.getErrors();
		assertEquals(1, errors.size());

		ResponseError error = errors.get(0);
		assertTrue(error.getMessage().contains("add a SourceLocation"), "The error message is: " + error.getMessage());

		List<SourceLocation> locations = error.getLocations();
		assertEquals(2, locations.size());
		//
		assertEquals(11, locations.get(0).getLine());
		assertEquals(111, locations.get(0).getColumn());
		assertEquals(null, locations.get(0).getSourceName(),
				"Should be 'Some source line 11', but it is cleared on server side, somewhere on the process");
		//
		assertEquals(22, locations.get(1).getLine());
		assertEquals(222, locations.get(1).getColumn());
		assertEquals(null, locations.get(1).getSourceName(),
				"Should be 'Another source name', but it is cleared on server side, somewhere on the process");
	}

	/**
	 * Check the Extension, that is added by the MyInstrumentation class, in the server project
	 * 
	 * @throws GraphQLRequestExecutionException
	 * @throws GraphQLRequestPreparationException
	 */
	@Test
	@Execution(ExecutionMode.CONCURRENT)
	// @Disabled // Found no way to define the error extension on server side, with graphql-java
	void test_ErrorExtension() {
		// Go, go, go
		GraphQLRequestExecutionException ex = assertThrows(GraphQLRequestExecutionException.class,
				() -> this.myQuery.exec("{error(errorLabel:\"add an extension\"){}}"));

		assertTrue(ex.getMessage().contains("add an extension"), "The exception message is: " + ex.getMessage());

		List<ResponseError> errors = ex.getErrors();
		assertEquals(1, errors.size());

		ResponseError error = errors.get(0);
		assertTrue(error.getMessage().contains("add an extension"), "The error message is: " + error.getMessage());

		Map<String, Object> extensions = error.getExtensions();
		assertNotNull(extensions);
		//
		assertEquals("An error extension's value (MyInstrumentation)", extensions.get("An error extension"));
		assertEquals("Another error extension's value (MyInstrumentation)", extensions.get("Another error extension"));
	}

	/**
	 * Check the SourceLocation, that is added by the MyInstrumentation class, in the server project
	 * 
	 * @throws GraphQLRequestExecutionException
	 * @throws GraphQLRequestPreparationException
	 */
	@Test
	@Execution(ExecutionMode.CONCURRENT)
	void test_ExceptionGetDataParsed() throws GraphQLRequestExecutionException, GraphQLRequestPreparationException {

		// Go, go, go

		// When the parameter for the error query is "add a SourceLocation", then a special management should be
		// executed on server side, for exception management. Let's test it.
		GraphQLRequestExecutionException ex = assertThrows(GraphQLRequestExecutionException.class,
				() -> this.myQuery.exec("{error(errorLabel:\"some exception message\"){} withoutParameters}"));

		// We should have received the relevant message.
		assertTrue(ex.getMessage().contains("some exception message"), "The exception message is: " + ex.getMessage());

		// The data part of the response should have been read
		assertNotNull(ex.getResponse());
		assertNotNull(ex.getData());
		assertTrue(ex.getData() instanceof CTP_MyQueryType_CTS, "ex.getData():" + ex.getData().getClass().getName());
		assertNotNull(ex.getData() instanceof CTP_MyQueryType_CTS, "ex.getData():" + ex.getData().getClass().getName());
		List<CIP_Character_CIS> withoutParameters = ((CTP_MyQueryType_CTS) ex.getData()).getWithoutParameters();
		assertNotNull(withoutParameters);
		assertTrue(withoutParameters.size() > 0);
		assertTrue(
				withoutParameters.get(0) instanceof CTP_Droid_CTS || withoutParameters.get(0) instanceof CTP_Human_CTS);

	}
}
