# Paidy forex proxy exercise

This is a local proxy that queries the one-frame API for current foreign exchange rates and provides these rates to other services via a REST API.

# How to use

Please have the one-frame API docker image running and available on localhost:8080.  Clone the repo and run the following from within the paidy-forex-proxy directory:

mvn install

## Starting

To start the service, use mvn spring-boot:start

## Stopping

To stop the service, use mvn spring-boot:stop

# API

The service can be accessed via URLs formatted like the following:

http://<HOSTNAME>:<PORT>/forex-rate?from=<CURRENCY>&to=<CURRENCY>

For example, assuming the service is available on 8081 locally, the following URL will provide rates for exchanging Japanese yen to Australian dollars:

http://localhost:8081/forex-rate?from=JPY&to=AUD

The following will be returned as a JSON object when queries are made:

**from**:		The currency to be traded from, as an uppercase string.
**to**:			The currency to be traded to, as an uppercase string.
**bid**:		The current bid price (The price at which purchasers of the currency pair are willing to buy, denominated in terms of the from: currency.)
**ask**:		The current asking price (The price at which sellers of the currency pair are willing to sell, denominated in terms of the from: currency.)
**price**:  		The market price.  (This will always be above the bidding price and below the asking price.)
**timestamp**:		The timestamp provided by the one-frame API for the rate quote.
**error**:		In case of any issues encountered fulfilling the query, the error text will be provided as a string.

If a result is not available that is less than five minutes old, a JSON object with error text and empty timestamp, bid, ask, and price will be returned.
	
# Story

*As a trader,
I want to query the forex-proxy service,
So that I can retrieve the latest currency rates.*

# Requirements

  This is a minimum viable product (MVP).  *An internal user of the application should be able to ask for an exchange rate between 2 given currencies and get back a rate that is not older than 5 minutes. The application should at least support 10,000 requests per day.*

“In practice, this should require the following 2 points:
1.	Fetch exchange rates from the one-frame API (https://hub.docker.com/r/paidyinc/one-frame) - supported currencies are:
	AUD, CAD, CHF, EUR, GBP, NZD, JPY, SGD, USD
2.	Make sure that downstream users of the service get descriptive errors in case
something goes wrong.”

# Limitations
	
	The one-frame API has the following limits:

	*One-frame service supports a maximum of 1000 requests per day on the GET /rates route*

	An implicit requirement is that we need to flag to the user when a non-stale result is unavailable (e.g. the one-frame service has been down for > 5 minutes)

# Possible approaches considered

  For now, as this is a minimum viable product meant to be run locally, this can be modeled with a single instance.  We will not consider issues such as load balancing or global distribution for now, HOWEVER, we should still model or at least plan for a failover approach to ensure reliability.
To avoid loading the API, we should maintain a cache of queried currency pairs and results which should be periodically refreshed within 5 minutes as long as they qualify for the cache.  Let’s explore some approaches:

1.	Keep all pairs in the cache at all times, refreshing every five minutes.
  With eight currencies as provided in the requirements chosen two at a time, this will mean 28 pairs maintained in the cache:
  8!
  ―――――― = 28
  2! * (8 – 2)!
  Assuming queries every five minutes, this means 28 pairs queried 12/hour, for a total of 28 * 12 * 24 = 8064 queries per day.  This will overload the one-frame API by a factor of 8 and is unacceptable as a solution.  Given the requirement of supporting 10,000 queries a day, it is likely that a lot of the pairs would go unqueried for significant periods of time.
2.	On the other hand, we could decide not to maintain a cache at all and simply forward queries directly.  However, our requirement of 10,000 queries/day would also quickly overload the one-frame API.
3.	Keep recently queried pairs in the cache for a period of (X >= 5) minutes.  We need to strive to conserve as many incoming queries as possible (at a minimum, no duplicate queries within 5 minutes) while also minimizing the number of queries that merely update data that subsequently does not get used to answer further queries.  Let us leverage the likelihood that some pairs are going to get queried more than others (and at different times of day depending on the waking hours of the countries using those currencies).  How long is it useful to maintain pairs in the cache for?
    A.	We can expire cache entries immediately after 5 minutes and update as soon as another query comes in.  As a MVP that needs to conserve query bandwidth, this is the safest approach.  A drawback of not aggressively adding pairs to the cache before they get queried is that we lose the ability to gap intermittent outages < 5 minutes long if the one-frame service suffers from availability problems.  This is an acceptable risk given the API limitations and loading requirements above.
    B.  Or, we could maintain pairs for longer than five minutes up to some period of X minutes.  This adds the risk of increasing the number of queries simply to refresh data that doesn’t get used, but may be useful in maintaining a value in cases where an intermittent outage occurs that resolves within five minutes.  For the purpose of creating an MVP that prioritizes not burdening the one-frame service, we will choose the former approach (expire cache entries after five minutes).

We should make this approach dynamic and easy to adjust (don’t hard code the minutes, currencies, etc, but have them read from a configuration file)

# Assumptions made

1.	The requirement that results not be older than five minutes can be considered ambiguous.  What if there is a time discrepancy between the local instance and the remote service's timestamp?  In the end, rather than using the one-frame API's timestamp, it seemed most performant to track the timestamp of results locally to enforce that results be no more than five minutes past the time the one-frame API was last queried for that pair.  However, we will provide the user with the timestamp given by the one-frame API, since that would be considered authoritative as part of the market quote.  This avoids providing the consuming service a timestamp that differs significantly from the market's clock.
2.	Although not specified in the task description, I assumed that services would want to know all of the information provided by the one-frame API (bid, ask, timestamp, in addition to price) as a solution failing to provide all of that would not be feature-complete from an investor perspective.
	    
# Testing scenarios to consider

1.	Pessimistic.  We engineer queries round-robinning the currencies and hitting them at least five minutes after the previous query for that pair.  This will hit them after the staleness requirement and try to max out the daily query limit for the one-frame API.  I expect given the chosen design decisions above this WILL trigger the per-day limit of one-frame, leading to the extended one-frame unavailability scenario.
2.	Atypical queries (e.g. JPY-JPY exchange rate, this can be considered a legitimate query that should return an exchange rate of “1”), non-supported currency pairs, empty pair parameters, etc.
3.	Multiple queries of the same pair within a ten-minute interval, attempt to ascertain whether the five-minute staleness threshold is met
4.	Overload (blast through the entire 10,000 query requirement in as little time as possible)
5.	Artificially limited queries (e.g. super slow read rate, packet loss)
6.	Intermittent unavailability of one-frame service under/over five minutes testing how staleness is handled.
