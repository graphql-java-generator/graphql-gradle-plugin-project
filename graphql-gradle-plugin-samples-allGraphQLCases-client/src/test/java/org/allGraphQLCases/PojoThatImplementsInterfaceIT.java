/**
 * 
 */
package org.allGraphQLCases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.allGraphQLCases.client.CTP_Foo140_CTS;
import org.allGraphQLCases.client.CIP_IBar1_CIS;
import org.allGraphQLCases.client.CIP_IBar12_CIS;
import org.allGraphQLCases.client.CIP_IBar2_CIS;
import org.allGraphQLCases.client.CIP_IFoo1_CIS;
import org.allGraphQLCases.client.CTP_TBar1_CTS;
import org.allGraphQLCases.client.CTP_TBar12_CTS;
import org.allGraphQLCases.client.CTP_TBar2_CTS;
import org.allGraphQLCases.client.CTP_TFoo1_CTS;
import org.allGraphQLCases.client.CTP_TFoo12_CTS;
import org.allGraphQLCases.client.MyQueryTypeExecutorAllGraphQLCases;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.graphql_java_generator.exception.GraphQLRequestExecutionException;
import com.graphql_java_generator.exception.GraphQLRequestPreparationException;

/**
 * This JUnit test class execute tests on the generated code. It should be in the graphql-maven-plugin-project, but this
 * would need to add the code generated by the Junit test to be part of the test-compilation goal, which is not
 * possible.
 * 
 * @author etienne-sf
 */
// Adding "webEnvironment = SpringBootTest.WebEnvironment.NONE" avoid this error:
// "No qualifying bean of type 'ReactiveClientRegistrationRepository' available"
// More details here: https://stackoverflow.com/questions/62558552/error-when-using-enablewebfluxsecurity-in-springboot
@SpringBootTest(classes = SpringTestConfig.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Execution(ExecutionMode.CONCURRENT)
public class PojoThatImplementsInterfaceIT {

	@Autowired
	MyQueryTypeExecutorAllGraphQLCases queryType;

	@Test
	void testSetterForTypeThatImplementsOneInterface() {
		CIP_IFoo1_CIS ifoo1 = new CTP_TFoo1_CTS();
		CTP_TFoo1_CTS tfoo1 = new CTP_TFoo1_CTS();
		CTP_TBar1_CTS tbar1 = new CTP_TBar1_CTS();
		CIP_IBar1_CIS ibar1 = new CTP_TBar1_CTS();

		//////////////////////////////////////////////////////////////////////
		// Setter for IFoo1
		//////////////////////////////////////////////////////////////////////

		assertNull(ifoo1.getBar());

		ifoo1.setBar(tbar1);
		assertEquals(tbar1, ifoo1.getBar());

		ifoo1.setBar(ibar1);
		assertEquals(ibar1, ifoo1.getBar());

		// Issue #124: setting a field to null raise an exception
		ifoo1.setBar(null);
		assertNull(ifoo1.getBar());

		//////////////////////////////////////////////////////////////////////
		// Setter for TFoo1
		//////////////////////////////////////////////////////////////////////

		assertNull(tfoo1.getBar());

		tfoo1.setBar(tbar1);
		assertEquals(tbar1, tfoo1.getBar());

		tfoo1.setBar(ibar1);
		assertEquals(ibar1, tfoo1.getBar());

		// Check of issue #124 correction: setting a field to null should not raise an exception
		tfoo1.setBar(null);
		assertNull(tfoo1.getBar());
	}

	@Test
	void testSetterForTypeThatImplementsTwoInterfaces() {
		IllegalArgumentException e;

		CIP_IFoo1_CIS ifoo1 = new CTP_TFoo1_CTS();
		CTP_TFoo1_CTS tfoo1 = new CTP_TFoo1_CTS();
		CTP_TFoo12_CTS ifoo12 = new CTP_TFoo12_CTS();
		CTP_TFoo12_CTS tfoo12 = new CTP_TFoo12_CTS();

		CTP_TBar1_CTS tbar1 = new CTP_TBar1_CTS();
		CIP_IBar1_CIS ibar1 = new CTP_TBar1_CTS();
		CTP_TBar2_CTS tbar2 = new CTP_TBar2_CTS();
		CIP_IBar2_CIS ibar2 = new CTP_TBar2_CTS();
		CTP_TBar12_CTS tbar12 = new CTP_TBar12_CTS();
		CIP_IBar12_CIS ibar12 = new CTP_TBar12_CTS();

		//////////////////////////////////////////////////////////////////////
		// Setter for IFoo1
		//////////////////////////////////////////////////////////////////////
		assertNull(ifoo1.getBar());

		ifoo1.setBar(tbar1);
		assertEquals(tbar1, ifoo1.getBar());
		ifoo1.setBar(ibar1);
		assertEquals(ibar1, ifoo1.getBar());

		e = assertThrows(IllegalArgumentException.class, () -> ifoo1.setBar(tbar2));
		assertTrue(e.getMessage().contains("TBar1"));
		e = assertThrows(IllegalArgumentException.class, () -> ifoo1.setBar(ibar2));
		assertTrue(e.getMessage().contains("TBar1"));

		e = assertThrows(IllegalArgumentException.class, () -> ifoo1.setBar(tbar12));
		assertTrue(e.getMessage().contains("TBar1"));
		e = assertThrows(IllegalArgumentException.class, () -> ifoo1.setBar(ibar12));
		assertTrue(e.getMessage().contains("TBar1"));

		// Check of issue #124 correction: setting a field to null should not raise an exception
		ifoo1.setBar(null);
		assertNull(ifoo1.getBar());

		//////////////////////////////////////////////////////////////////////
		// Setter for TFoo1
		//////////////////////////////////////////////////////////////////////
		assertNull(tfoo1.getBar());

		tfoo1.setBar(tbar1);
		assertEquals(tbar1, tfoo1.getBar());
		tfoo1.setBar(ibar1);
		assertEquals(ibar1, tfoo1.getBar());

		e = assertThrows(IllegalArgumentException.class, () -> tfoo1.setBar(tbar2));
		assertTrue(e.getMessage().contains("TBar1"));
		e = assertThrows(IllegalArgumentException.class, () -> tfoo1.setBar(ibar2));
		assertTrue(e.getMessage().contains("TBar1"));

		e = assertThrows(IllegalArgumentException.class, () -> tfoo1.setBar(tbar12));
		assertTrue(e.getMessage().contains("TBar1"));
		e = assertThrows(IllegalArgumentException.class, () -> tfoo1.setBar(ibar12));
		assertTrue(e.getMessage().contains("TBar1"));

		// Check of issue #124 correction: setting a field to null should not raise an exception
		tfoo1.setBar(null);
		assertNull(tfoo1.getBar());

		//////////////////////////////////////////////////////////////////////
		// Setter for IFoo12
		//////////////////////////////////////////////////////////////////////
		assertNull(ifoo12.getBar());

		e = assertThrows(IllegalArgumentException.class, () -> ifoo12.setBar(tbar1));
		assertTrue(e.getMessage().contains("TBar12"));
		e = assertThrows(IllegalArgumentException.class, () -> ifoo12.setBar(ibar1));
		assertTrue(e.getMessage().contains("TBar12"));

		e = assertThrows(IllegalArgumentException.class, () -> ifoo12.setBar(tbar2));
		assertTrue(e.getMessage().contains("TBar12"));
		e = assertThrows(IllegalArgumentException.class, () -> ifoo12.setBar(ibar2));
		assertTrue(e.getMessage().contains("TBar12"));

		ifoo12.setBar(tbar12);
		assertEquals(tbar12, ifoo12.getBar());
		ifoo12.setBar(ibar12);
		assertEquals(ibar12, ifoo12.getBar());

		// Check of issue #124 correction: setting a field to null should not raise an exception
		ifoo12.setBar(null);
		assertNull(ifoo12.getBar());

		//////////////////////////////////////////////////////////////////////
		// Setter for TFoo12
		//////////////////////////////////////////////////////////////////////
		assertNull(tfoo12.getBar());

		e = assertThrows(IllegalArgumentException.class, () -> tfoo12.setBar(tbar1));
		assertTrue(e.getMessage().contains("TBar12"));
		e = assertThrows(IllegalArgumentException.class, () -> tfoo12.setBar(ibar1));
		assertTrue(e.getMessage().contains("TBar12"));

		e = assertThrows(IllegalArgumentException.class, () -> tfoo12.setBar(tbar2));
		assertTrue(e.getMessage().contains("TBar12"));
		e = assertThrows(IllegalArgumentException.class, () -> tfoo12.setBar(ibar2));
		assertTrue(e.getMessage().contains("TBar12"));

		tfoo12.setBar(tbar12);
		assertEquals(tbar12, tfoo12.getBar());
		tfoo12.setBar(ibar12);
		assertEquals(ibar12, tfoo12.getBar());

		// Check of issue #124 correction: setting a field to null should not raise an exception
		tfoo12.setBar(null);
		assertNull(tfoo12.getBar());
	}

	@Test
	void testIssue140() throws GraphQLRequestExecutionException, GraphQLRequestPreparationException {
		CTP_Foo140_CTS foo = queryType.foo140("{bar}");
		assertEquals("Bar140's name for a Foo140", foo.getBar().getName());
	}

}
