/**
 * 
 */
package graphql.gradleplugin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import graphql.gradleplugin.Dummy;

/**
 * @author EtienneSF
 *
 */
class DummyTest {

	Dummy graphqlGradlePlugin;

	@BeforeEach
	public void setup() {
		graphqlGradlePlugin = new Dummy();
	}

	/**
	 * Test method for {@link graphql.gradleplugin.Dummy#plus(int, int)}.
	 */
	@Test
	void testPlus() {
		assertEquals(8, graphqlGradlePlugin.plus(3, 5));
	}

}
