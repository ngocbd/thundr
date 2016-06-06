package com.threewks.thundr.route.controller;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.threewks.thundr.http.Header;
import com.threewks.thundr.request.mock.MockRequest;

public class FilterRulesTest {
	private MockRequest req = new MockRequest();

	@Test
	public void shouldPermitEverythingByDefault() {
		req.withHeader(Header.UserAgent, "Googlebot/2.1 (+http://www.google.com/bot.html)");
		FilterRules filterRules = new FilterRules();
		assertThat(filterRules.shouldIgnore(req), is(false));
	}

	@Test
	public void shouldIgnoreSpecifiedUserAgent() {
		req.withHeader(Header.UserAgent, "Googlebot/2.1 (+http://www.google.com/bot.html)");
		FilterRules filterRules = new FilterRules();
		assertThat(filterRules.shouldIgnore(req), is(false));

		filterRules.ignoreUserAgents("Googlebot");

		assertThat(filterRules.shouldIgnore(req), is(true));
	}

	@Test
	public void shouldIgnoreUserAgentsWithSpecifiedStrings() {
		req.withHeader(Header.UserAgent, "Googlebot/2.1 (+http://www.google.com/bot.html)");
		FilterRules filterRules = new FilterRules();
		filterRules.ignoreUserAgents("Pinterest", "Googlebot");

		assertThat(filterRules.shouldIgnore(req), is(true));
	}

	@Test
	public void shouldAllowNormalAgentNotIncludedInIgnoreList() {
		req.withHeader(Header.UserAgent, "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.80 Safari/537.36");
		FilterRules filterRules = new FilterRules();
		filterRules.ignoreUserAgents("Pinterest", "Googlebot");

		assertThat(filterRules.shouldIgnore(req), is(false));
		
		filterRules.ignoreCommonBots();
		assertThat(filterRules.shouldIgnore(req), is(false));
	}

	@Test
	public void shouldIgnoreCommonBots() {
		FilterRules rules = new FilterRules().ignoreCommonBots();
		assertThat(rules.getIgnoreUserAgents(), hasItem("Googlebot"));
		assertThat(rules.getIgnoreUserAgents(), hasItem("FeedFetcher-Google"));
		assertThat(rules.getIgnoreUserAgents(), hasItem("bingbot"));
		assertThat(rules.getIgnoreUserAgents(), hasItem("facebookexternalhit"));
		assertThat(rules.getIgnoreUserAgents(), hasItem("Facebot"));
		assertThat(rules.getIgnoreUserAgents(), hasItem("Twitterbot"));
		assertThat(rules.getIgnoreUserAgents(), hasItem("Pinterest"));
		assertThat(rules.getIgnoreUserAgents(), hasItem("LinkedInBot"));
		assertThat(rules.getIgnoreUserAgents(), hasItem("ia_archiver"));
		assertThat(rules.getIgnoreUserAgents(), hasItem("Pingdom.com_bot"));
		assertThat(rules.getIgnoreUserAgents(), hasItem("UptimeRobot"));
		assertThat(rules.getIgnoreUserAgents(), hasItem("Stackdriver_terminus_bot"));
	}

}
