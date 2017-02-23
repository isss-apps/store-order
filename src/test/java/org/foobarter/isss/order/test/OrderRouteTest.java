package org.foobarter.isss.order.test;

import org.apache.camel.CamelContext;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.assertj.core.api.Assertions;
import org.foobarter.isss.order.OrderReceipt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrderRouteTest {

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private CamelContext camelContext;

	private final String restService = "http://localhost:8080";

	@Test
	public void putNewOrderTest() {
		String requestJson = "{\n" +
				"  \"uuid\": \"1\",\n" +
				"  \"customer\": {\n" +
				"    \"name\": \"Random F. Flyer\",\n" +
				"    \"address\": \"Somewhere in the Galaxy\"\n" +
				"  },\n" +
				"  \"items\": [\n" +
				"    {\n" +
				"      \"storeId\": 1000,\n" +
				"      \"amount\": 2,\n" +
				"      \"itemPrice\": \"19.99\"\n" +
				"    }\n" +
				"  ]\n" +
				"}\n";

		ResponseEntity<OrderReceipt> exchange = restTemplate.exchange("http://localhost:8080/order", HttpMethod.PUT, new HttpEntity<>(requestJson), OrderReceipt.class);
		Assertions.assertThat(exchange.getStatusCodeValue()).isEqualTo(200);
		Assertions.assertThat(exchange.getBody().getMessage()).isEqualTo("Thank you Random F. Flyer for your order!");
	}
}
