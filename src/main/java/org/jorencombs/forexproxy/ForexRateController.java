package org.jorencombs.forexproxy;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Controls the /forex-rate path.  Responses will be {@link ForexRate} JSONs.
 */

@RestController
public class ForexRateController {
    private static final String template = "Rate: ";

    @GetMapping("/forex-rate")
    /**
     * Looks up current rates for trading from one currency to another.  Guaranteed to be accurate within five minutes.
     *
     * @param from - The currency being traded from.
     * @param to - The currency being traded to
     * @return ForexRate - A JSON object with the returned rate.
     *
     * FIXME:  Currently passthrough implementation only!!!
     */
    public ForexRate forexRate(@RequestParam(value = "from", defaultValue = "USD") String from, @RequestParam(value = "to", defaultValue = "JPY") String to) {
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
    }
}
