/**
 * 
 */
package graphql.graphqlplugin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author EtienneSF
 *
 */
class GraphqlGradlePluginTest {

	GraphqlGradlePlugin graphqlGradlePlugin;

	@BeforeEach
	public void setup() {
		graphqlGradlePlugin = new GraphqlGradlePlugin();
	}

	/**
	 * Test method for {@link graphql.graphqlplugin.GraphqlGradlePlugin#plus(int, int)}.
	 */
	@Test
	void testPlus() {
		assertEquals(8, graphqlGradlePlugin.plus(3, 5));
	}

}
