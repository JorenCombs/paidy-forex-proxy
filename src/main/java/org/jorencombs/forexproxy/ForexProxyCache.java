package org.jorencombs.forexproxy;

import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.*;

public class ForexProxyCache {

    private class Result {
        public Result (Future<ForexRate> future, long timestamp) {
            this.future = future;
            this.timestamp = timestamp;
        }

        Future<ForexRate> future;
        long timestamp;
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

    public ForexRate getForexRate(final String from, final String to) {
        String pair = from + to;
        synchronized(cache) {
            if (!cache.containsKey(pair)
                    || System.currentTimeMillis() - cache.get(pair).timestamp > ForexProxyApplication.STALENESS_LIMIT) {
                Future<ForexRate> future = executor.submit(() -> {
                    ForexRate forexRate = new ForexRate(from, to);
                    WebClient webClient = WebClient.create();
                    OneFrameResponse[] response = webClient.get()
                            .uri("localhost:8080/rates?pair=" + from + to)
                            .header("token", "10dc303535874aeccc86a8251e6992f5")
                            .retrieve()
                            .bodyToMono(OneFrameResponse[].class)
                            .block();

                    if (response != null && response.length > 0) {
                        forexRate.from = String.valueOf(response[0].from);
                        forexRate.to = String.valueOf(response[0].to);
                        forexRate.ask = Double.valueOf(response[0].ask);
                        forexRate.bid = Double.valueOf(response[0].bid);
                        forexRate.price = Double.valueOf(response[0].price);
                        forexRate.timestamp = String.valueOf(response[0].time_stamp);
                    }
                    return forexRate;
                });
                Result result = new Result(future, System.currentTimeMillis());
                cache.put(pair, result);
            }
        }
        try {
            return cache.get(pair).future.get(5000, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            // Don't print timeouts... could get spammy
            return new ForexRate(from, to, "Timeout while waiting for quote");
        } catch (Exception e) {
            e.printStackTrace();
            return new ForexRate(from, to, "Interrupted while waiting for quote");
        }
    }
}
