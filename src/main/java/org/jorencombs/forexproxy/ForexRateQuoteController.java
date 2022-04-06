package org.jorencombs.forexproxy;

import org.jsoup.safety.Safelist;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.jsoup.Jsoup;

import java.util.Arrays;

/**
 * Controls the /forex-rate path.  Responses will be {@link ForexRateQuote} JSONs.
 */

@RestController
public class ForexRateQuoteController {

    @GetMapping("/forex-rate")
    /**
     * Looks up current rates for trading the specified currency pair.  Guaranteed to be accurate within five minutes.
     *
     * @param from - The currency being traded from.
     * @param to - The currency being traded to
     * @return ForexRateQuote - A JSON object with the returned rate.
     */
    public ForexRateQuote forexRate(@RequestParam(value = "from", defaultValue = "USD") String from, @RequestParam(value = "to", defaultValue = "JPY") String to) {
        String sanitizedFrom = Jsoup.clean(from, Safelist.simpleText()).toUpperCase(),
                sanitizedTo = Jsoup.clean(to, Safelist.simpleText()).toUpperCase();
        if (ForexProxyApplication.SUPPORTED_CURRENCIES.contains(sanitizedFrom)
                && ForexProxyApplication.SUPPORTED_CURRENCIES.contains(sanitizedTo)) {
            if (!sanitizedFrom.equals(sanitizedTo)) {
                return ForexProxyCache.getInstance().getForexRate(sanitizedFrom, sanitizedTo);
            } else {
                return new ForexRateQuote(sanitizedFrom, sanitizedTo, "From and to currencies cannot be the same");
            }
        } else {
            return new ForexRateQuote(sanitizedFrom, sanitizedTo,
                    "Both currencies must be one of the following: "
                            + Arrays.toString(ForexProxyApplication.SUPPORTED_CURRENCIES.toArray()));
        }
    }
}
