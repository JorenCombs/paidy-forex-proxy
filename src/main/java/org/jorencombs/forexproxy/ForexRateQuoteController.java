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
     * Looks up current rates for trading the specified currency pair.  Guaranteed to be accurate within
     * {@link ForexProxyApplication#STALENESS_LIMIT}
     *
     * @param from - The currency being traded from.  This is not case sensitive.  Must be in
     *      {@link ForexProxyApplication#SUPPORTED_CURRENCIES} or an error will be returned.
     * @param to - The currency being traded to.  This is not case sensitive.  Must be in
     *      {@link ForexProxyApplication#SUPPORTED_CURRENCIES} or an error will be returned.
     * @return ForexRateQuote - A JSON object with the returned rate.  In the event of an error,
     * a ForexRateQuote object will be returned that has bid, ask, and price of zero and will
     * have error text describing why the request could not be fulfilled.  The to/from currencies
     * will remain as supplied by the user.
     */
    public ForexRateQuote forexRate(@RequestParam(value = "from", defaultValue = "") String from, @RequestParam(value = "to", defaultValue = "") String to) {
        String sanitizedFrom = Jsoup.clean(from, Safelist.simpleText()).toUpperCase(),
                sanitizedTo = Jsoup.clean(to, Safelist.simpleText()).toUpperCase();
        if (ForexProxyApplication.SUPPORTED_CURRENCIES.contains(sanitizedFrom)
                && ForexProxyApplication.SUPPORTED_CURRENCIES.contains(sanitizedTo)) {
            if (!sanitizedFrom.equals(sanitizedTo)) {
                return ForexProxyCache.getInstance().getForexRate(sanitizedFrom, sanitizedTo);
            } else {
                return new ForexRateQuote(sanitizedFrom, sanitizedTo, "Currencies specified in 'from' and 'to' parameters cannot be the same");
            }
        } else {
            return new ForexRateQuote(sanitizedFrom, sanitizedTo,
                    "currencies specified in 'from' and 'to' parameters must be one of the following: "
                            + Arrays.toString(ForexProxyApplication.SUPPORTED_CURRENCIES.toArray()));
        }
    }
}
