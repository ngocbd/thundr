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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.threewks.thundr.http.Header;
import com.threewks.thundr.request.Request;

public class FilterRules {
	protected List<String> ignoreUserAgents = new ArrayList<>();
	private String ignoreAgentsRegex = null;

	public FilterRules() {

	}

	/**
	 * The {@link SessionFilter} will ignore user agents matching the given substrings.
	 * Multiple calls will add more entries.
	 * 
	 * @param substring
	 * @return
	 */
	public FilterRules ignoreUserAgents(String... substring) {
		if (substring != null && substring.length > 0) {
			ignoreUserAgents.addAll(Arrays.asList(substring));
		}
		ignoreAgentsRegex = null;
		return this;
	}

	public List<String> getIgnoreUserAgents() {
		return ignoreUserAgents;
	}

	public boolean shouldIgnore(Request req) {
		return userAgentIsBlocked(req.getHeader(Header.UserAgent));
	}

	public FilterRules ignoreCommonBots() {
		ignoreUserAgents("Googlebot", "FeedFetcher-Google"); // Google
		ignoreUserAgents("bingbot"); // Bing
		ignoreUserAgents("facebookexternalhit", "Facebot"); // Facebook
		ignoreUserAgents("Twitterbot"); // Twitter
		ignoreUserAgents("Pinterest"); // Pinterest
		ignoreUserAgents("LinkedInBot"); // LinkedIn
		ignoreUserAgents("ia_archiver"); // Alex
		ignoreUserAgents("Pingdom.com_bot"); // Pingdom
		ignoreUserAgents("UptimeRobot"); // UptimeRobot
		ignoreUserAgents("Stackdriver_terminus_bot"); // StackDriver
		return this;
	}

	protected boolean userAgentIsBlocked(String userAgent) {
		if (ignoreAgentsRegex == null) {
			ignoreAgentsRegex = ignoreUserAgents.isEmpty() ? "" : ".*(" + StringUtils.join(ignoreUserAgents, "|") + ").*";
		}
		return ignoreAgentsRegex.length() == 0 ? false : userAgent.matches(ignoreAgentsRegex);
	}

}
