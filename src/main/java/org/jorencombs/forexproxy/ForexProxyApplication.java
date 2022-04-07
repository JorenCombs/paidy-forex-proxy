package org.jorencombs.forexproxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@SpringBootApplication
public class ForexProxyApplication {

	/**
	 * How many threads to use for one-frame queries.
	 */
	public final static int ONE_FRAME_THREADS = 10;

	/**
	 * Timeout for one-frame queries
	 */
	public final static int ONE_FRAME_TIMEOUT = 5000;

	/**
	 * How long to hold on to the last rate quote before requerying the one-frame API.
	 * This is in milliseconds (300000 = five minutes)
	 */
	public final static int STALENESS_LIMIT = 300000; // Five minutes

	/**
	 * How long to hold on to the last rate quote before requerying the one-frame API
	 * if an error occurs.  This is in milliseconds (10000 = ten seconds)
	 */
	public final static int ERROR_STALENESS_LIMIT = 10000; // Ten seconds


	/**
	 * The list of supported currencies.  Must be all-caps, or queries for that currency
	 * will not be recognized.  Queries for currencies not in this list will be rejected.
	 */
	public final static Set SUPPORTED_CURRENCIES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
			"AUD", "CAD", "CHF", "EUR", "GBP", "NZD", "JPY", "SGD", "USD")));


	public static void main(String[] args) {
		SpringApplication.run(ForexProxyApplication.class, args);
	}

}
