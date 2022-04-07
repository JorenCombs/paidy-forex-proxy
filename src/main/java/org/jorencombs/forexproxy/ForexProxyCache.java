package org.jorencombs.forexproxy;

import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.*;

public class ForexProxyCache {

    private class Result {
        public Result (Future<ForexRateQuote> future, long expiration) {
            this.future = future;
            this.expiration = expiration;
        }

        /**
         * Future used for retrieving query results
         */
        Future<ForexRateQuote> future;

        /**
         * System time (in millis) past which this should be expired
         */
        long expiration;
    }

    /**
     * Private constructor to avoid instantiating this class outside of our getInstance() call
     */
    private ForexProxyCache() {
    }

    /**
     * The shared instance we will use for all {@link #getInstance()} calls
     */
    private static ForexProxyCache INSTANCE;

    /**
     * Use this to interact with the forex proxy cache.
     * @return - The shared instance of the forex proxy cache
     */
    public synchronized static ForexProxyCache getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ForexProxyCache();
        }
        return INSTANCE;
    }

    private ConcurrentMap<String, Result> cache = new ConcurrentHashMap<String, Result>();

    private ExecutorService executor = Executors.newFixedThreadPool(ForexProxyApplication.ONE_FRAME_THREADS);

    public ForexRateQuote getForexRate(final String from, final String to) {
        String pair = from + to;
        Result result = null;
        /*
            Although the hashmap is concurrent, I think we still need the synchronized block
            because another thread could test to see if the result is null while we are still
            responding to our own check that it was indeed null.

            I am not as concerned about a separate thread writing to the result's expiration
            time after we process our check.  E.g. thread one gets a future, waits for result,
            thread two gets the same future and at this point, the future is within the
            staleness limit so thread two's staleness check succeeds.  But then thread one gets
            an error and updates the timeout to expire sooner, but thread two just returns the
            same future anyway.  Effectively, thread one and two's outcome is identical in that
            they were run more or less at the same time, and in both cases the user will get the
            same error.
         */
        synchronized(cache) {
            result = cache.get(pair);
            if (result == null
                    || System.currentTimeMillis() > result.expiration) {
                Future<ForexRateQuote> future = executor.submit(() -> {
                    ForexRateQuote forexRateQuote = new ForexRateQuote(from, to);
                    WebClient webClient = WebClient.create();
                    OneFrameResponse[] response = webClient.get()
                            .uri("localhost:8080/rates?pair=" + from + to)
                            .header("token", "10dc303535874aeccc86a8251e6992f5")
                            .retrieve()
                            .bodyToMono(OneFrameResponse[].class)
                            .block();

                    if (response != null && response.length > 0) {
                        forexRateQuote.from = String.valueOf(response[0].from);
                        forexRateQuote.to = String.valueOf(response[0].to);
                        forexRateQuote.ask = Double.valueOf(response[0].ask);
                        forexRateQuote.bid = Double.valueOf(response[0].bid);
                        forexRateQuote.price = Double.valueOf(response[0].price);
                        forexRateQuote.timestamp = String.valueOf(response[0].time_stamp);
                    }
                    return forexRateQuote;
                });
                result = new Result(future, System.currentTimeMillis() + ForexProxyApplication.STALENESS_LIMIT);
                cache.put(pair, result);
            }
        }
        // Result will not be null after it exits the synchronized block.
        try {
            return result.future.get(ForexProxyApplication.ONE_FRAME_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            result.expiration = Math.min(result.expiration, System.currentTimeMillis() + ForexProxyApplication.ERROR_STALENESS_LIMIT);
            System.err.println("Timeout while connecting to one-frame API");
            return new ForexRateQuote(from, to, "Timeout while connecting to one-frame API");
        } catch (Exception e) {
            result.expiration = Math.min(result.expiration, System.currentTimeMillis() + ForexProxyApplication.ERROR_STALENESS_LIMIT);
            System.err.println("Error while connecting to one-frame API: " + e.getClass().getSimpleName());
            e.printStackTrace();
            return new ForexRateQuote(from, to, "Error while connecting to one-frame API");
        }
    }
}
