package org.jorencombs.forexproxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@SpringBootApplication
public class ForexProxyApplication {

	public final static int ONE_FRAME_THREADS = 10;

	public final static int STALENESS_LIMIT = 300000; // Five minutes

	public final static Set SUPPORTED_CURRENCIES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
			"AUD", "CAD", "CHF", "EUR", "GBP", "NZD", "JPY", "SGD", "USD")));


	public static void main(String[] args) {
		SpringApplication.run(ForexProxyApplication.class, args);
	}

}
