package org.foobarter.isss.order;

import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.processor.idempotent.MemoryIdempotentRepository;
import org.apache.camel.spring.boot.FatJarRouter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OrderRoute extends FatJarRouter {

	// must have a main method spring-boot can run
	public static void main(String[] args) {
		FatJarRouter.main(args);
	}

	@Autowired
	private OrderProcessor orderProcessor;

    @Override
    public void configure() throws Exception {
		restConfiguration().component("netty4-http").host("0.0.0.0").port(8080).bindingMode(RestBindingMode.auto);

		rest("/order")
				.consumes("application/json").produces("application/json")

				.put().type(Order.class).outType(OrderReceipt.class)
					.route()
						.log("Body: ${body}")
						.idempotentConsumer(simple("${body.uuid}"),
							MemoryIdempotentRepository.memoryIdempotentRepository(1024))
							.skipDuplicate(false)
						.log("Processing order ${body.uuid} for ${body.customer.name}, ${body.customer.address}")
						.process(orderProcessor)
						.log("Receipt for ${body.price}: ${body.message}");

	}
}
