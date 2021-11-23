package org.allGraphQLCases.subscription.graphqlrepository;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.concurrent.TimeUnit;

import org.allGraphQLCases.SpringTestConfig;
import org.allGraphQLCases.subscription.SubscriptionCallbackListInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.graphql_java_generator.client.SubscriptionClient;
import com.graphql_java_generator.exception.GraphQLRequestExecutionException;

//Adding "webEnvironment = SpringBootTest.WebEnvironment.NONE" avoid this error:
//"No qualifying bean of type 'ReactiveClientRegistrationRepository' available"
//More details here: https://stackoverflow.com/questions/62558552/error-when-using-enablewebfluxsecurity-in-springboot
@SpringBootTest(classes = SpringTestConfig.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Execution(ExecutionMode.CONCURRENT)
public class FullRequestSubscriptionIT {

	@Autowired
	FullRequestSubscriptionGraphQLRepository graphQLRepo;

	@Execution(ExecutionMode.CONCURRENT)
	@Test
	void test_SubscribeToAList() throws GraphQLRequestExecutionException, InterruptedException {
		// Preparation
		SubscriptionCallbackListInteger callback = new SubscriptionCallbackListInteger(
				"FullRequestSubscriptionIT.test_SubscribeToAList");

		// Go, go, go
		SubscriptionClient sub = graphQLRepo.subscribeToAList(callback);

		// Verification
		// Let's wait a max of 20 second, until we receive some notifications
		callback.latchForMessageReception.await(20, TimeUnit.SECONDS);

		// Let's disconnect from the subscription
		sub.unsubscribe();

		// We should have received a notification from the subscription
		assertNotNull(callback.lastReceivedMessage);
	}

}
