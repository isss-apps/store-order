package org.foobarter.isss.order;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.processor.idempotent.MemoryIdempotentRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OrderRoute extends RouteBuilder {

	// must have a main method spring-boot can run
	public static void main(String[] args) {
		SpringApplication.run(OrderRoute.class, args);
	}

	@Value("${rest.host}")
	private String host;
	@Value("${rest.port}")
	private int port;

	@Autowired
	private OrderProcessor orderProcessor;

    @Override
    public void configure() throws Exception {
		restConfiguration().component("undertow").host(host).port(port).bindingMode(RestBindingMode.auto);

		getContext().getShutdownStrategy().setTimeout(10);

		rest("/order")
				.consumes("application/json").produces("application/json")

				.put().type(Order.class).outType(OrderReceipt.class)
					.route()
						.log("Body: ${body}")

						.choice() // simulated infinite delay
							.when(body().method("getCustomer").method("getName").isEqualToIgnoreCase("error"))
							.to("direct:timeout")
								.endChoice()
							.otherwise()
								.idempotentConsumer(simple("${body.uuid}"),
									MemoryIdempotentRepository.memoryIdempotentRepository(1024))
									.skipDuplicate(false)
								.log("Processing order ${body.uuid} for ${body.customer.name}, ${body.customer.address}")
								.process(orderProcessor)
								.log("Receipt for ${body.price}: ${body.message}");

		from("direct:timeout")
				.delay(60_000).asyncDelayed()
				.setHeader("CamelHttpResponseCode", simple("503"));

	}
}
