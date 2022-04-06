package org.jorencombs.forexproxy;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Intended to be used for responses from the one-frame service.
 */
public class OneFrameResponse {
    /**
     * The currency being traded from
     */
    @JsonProperty("from")
    String from;

    /**
     * The currency being traded to
     */
    @JsonProperty("to")
    String to;

    /**
     * The current bid price (by purchasers of the {@link #from}-{@link #to} currency pair, denominated in {@link #from} currency.)
     */
    @JsonProperty("bid")
    double bid;

    /**
     * The current ask price (by sellers of the {@link #from}-{@link #to} currency pair, denominated in {@link #from} currency.)
     */
    @JsonProperty("ask")
    double ask;

    /**
     * The current market price of the currency pair, denominated in {@link #from} currency.
     */
    @JsonProperty("price")
    double price;

    /**
     * The timestamp these rates were obtained at
     */
    @JsonProperty("time_stamp")
    String time_stamp;

}
