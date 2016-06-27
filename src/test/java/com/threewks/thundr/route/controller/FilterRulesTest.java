/*
 * This file is a component of thundr, a software library from 3wks.
 * Read more: http://3wks.github.io/thundr/
 * Copyright (C) 2015 3wks, <thundr@3wks.com.au>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
