package org.jorencombs.forexproxy;

/**
 * Used for responses to queries of our forex-proxy service.
 * These mostly mirror the values of {@link OneFrameResponse} but also includes a text field for errors.\
 */
public class ForexRateQuote {
    public ForexRateQuote(String from, String to) {
        this.from = from;
        this.to = to;
    }

    public ForexRateQuote(String from, String to, String error) {
        this.from = from;
        this.to = to;
        this.error = error;
    }

    /**
     * Gets the currency being traded from
     *
     * @return String - The currency the {@link #getPrice() price} will be denominated in terms of.
     */
    public String getFrom() {
        return from;
    }

    /**
     * Gets the currency being traded to
     *
     * @return String - The currency to search for rates to purchase denominated in terms of {@link #getFrom() the from currency}
     */
    public String getTo() {
        return to;
    }

    /**
     * Gets the current bid price.
     *
     * @return Double - The bid price (by purchaser of {@link #from}-{@link #to} currency pair, denominated in terms of {@link #getFrom() the from currency}.
     */
    public double getBid() {
        return bid;
    }

    /**
     * Gets the current ask price.
     *
     * @return Double - The ask price (by purchaser of {@link #from}-{@link #to} currency pair, denominated in terms of {@link #getFrom() the from currency}.
     */
    public double getAsk() {
        return ask;
    }

    /**
     * Gets the current market price.
     *
     * @return Double - The market price (by purchaser of {@link #from}-{@link #to} currency pair, denominated in terms of {@link #getFrom() the from currency}.
     */
    public double getPrice() {
        return price;
    }

    /**
     * Gets the timestamp these rates are valid as of, as determined by the market.
     * @return String - The timestamp, in epoch time millis
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Gets the error text, if any.  This should be used to log issues encountered fulfilling the request
     */
    public String getError() {
        return error;
    }

    /**
     * The currency being traded from
     */
    String from;

    /**
     * The currency being traded to
     */
    String to;

    /**
     * The current bid price (by purchaser of {@link #from}-{@link #to} currency pair, denominated in {@link #from} currency.)
     */
    double bid = 0;

    /**
     * The current ask price (by seller of {@link #from}-{@link #to} currency pair, denominated in {@link #from} currency.)
     */
    double ask = 0;

    /**
     * The current market price of {@link #to} currency, denominated in {@link #from} currency.
     */
    double price = 0;

    /**
     * The timestamp these rates were generated, according to the one-frame API.
     */
    String timestamp = "";

    /**
     * Errors encountered while fulfilling this request should be set here
     */
    String error = "";
}
