package org.foobarter.isss.order;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class OrderProcessor implements Processor {
	@Override
	public void process(Exchange exchange) throws Exception {
		Order order = exchange.getIn().getBody(Order.class);

		BigDecimal price = BigDecimal.ZERO;

		for (OrderItem item : order.getItems()) {
			price = price.add(item.getItemPrice().multiply(BigDecimal.valueOf(item.getAmount())));
		}

		OrderReceipt receipt = new OrderReceipt();
		receipt.setPrice(price);
		receipt.setMessage("Thank you " + order.getCustomer().getName() + " for your order!");

		if ((boolean)exchange.getProperty(Exchange.DUPLICATE_MESSAGE, false)) {
			// message is a duplicate, so we just generate the receipt
			receipt.setDuplicate(true);
		}
		else {
			// message is not a duplicate, we would really process the order here...
			receipt.setDuplicate(false);
		}



		exchange.getOut().setBody(receipt);
	}
}
