/*
 * This file is a component of thundr, a software library from 3wks.
 * Read more: http://www.3wks.com.au/thundr
 * Copyright (C) 2013 3wks, <thundr@3wks.com.au>
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
package com.threewks.thundr.view.negotiating.strategy;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.threewks.thundr.http.HttpSupport;
import com.threewks.thundr.test.mock.servlet.MockHttpServletRequest;
import com.threewks.thundr.view.json.JsonNegotiator;
import com.threewks.thundr.view.jsonp.JsonpNegotiator;
import com.threewks.thundr.view.negotiating.NegotiatingView;
import com.threewks.thundr.view.negotiating.Negotiator;
import com.threewks.thundr.view.negotiating.ViewNegotiatorRegistry;
import com.threewks.thundr.view.negotiating.ViewNegotiatorRegistryImpl;

public class AcceptsHeaderNegotiationStrategyTest {

	private MockHttpServletRequest req;
	private NegotiatingView view;
	private ViewNegotiatorRegistry viewNegotiatorRegistry;
	private AcceptsHeaderNegotiationStrategy strategy;
	@SuppressWarnings("rawtypes")
	private Negotiator jsonNegotiator;
	@SuppressWarnings("rawtypes")
	private Negotiator jsonpNegotiator;

	@Before
	public void before() {
		req = new MockHttpServletRequest();
		view = new NegotiatingView("test");
		viewNegotiatorRegistry = new ViewNegotiatorRegistryImpl();
		strategy = new AcceptsHeaderNegotiationStrategy();
		jsonNegotiator = new JsonNegotiator();
		jsonpNegotiator = new JsonpNegotiator();
	}

	@Test
	public void shouldReturnNegotiatorBasedOnAcceptHeader() {
		viewNegotiatorRegistry.addNegotiator("application/json", jsonNegotiator);

		req.header(HttpSupport.Header.Accept, "application/json");

		Negotiator<?> result = strategy.findNegotiator(req, view, viewNegotiatorRegistry);
		assertThat(result, is(jsonNegotiator));
	}

	@Test
	public void shouldReturnNegotiatedViewRespectingQualityParameters() {
		viewNegotiatorRegistry.addNegotiator("application/json", jsonNegotiator);
		viewNegotiatorRegistry.addNegotiator("application/javascript", jsonpNegotiator);

		req.header(HttpSupport.Header.Accept, "application/json;q=0.7,application/javascript;q=0.8");

		Negotiator<?> result = strategy.findNegotiator(req, view, viewNegotiatorRegistry);
		assertThat(result, is(jsonpNegotiator));
	}

	@Test
	public void shouldReturnNegotiatedViewAssumingNoQualityIsEquivalentToOne() {
		viewNegotiatorRegistry.addNegotiator("application/json", jsonNegotiator);
		viewNegotiatorRegistry.addNegotiator("application/javascript", jsonpNegotiator);

		req.header(HttpSupport.Header.Accept, "application/json;q=0.9,application/javascript");

		Negotiator<?> result = strategy.findNegotiator(req, view, viewNegotiatorRegistry);
		assertThat(result, is(jsonpNegotiator));
	}

	@Test
	public void shouldReturnNegotiatedViewIgnoringPreferredTypesThatArentSupported() {
		viewNegotiatorRegistry.addNegotiator("application/json", jsonNegotiator);
		viewNegotiatorRegistry.addNegotiator("application/javascript", jsonpNegotiator);

		req.header(HttpSupport.Header.Accept, "text/plain;q=1,application/json;q=0.9");

		Negotiator<?> result = strategy.findNegotiator(req, view, viewNegotiatorRegistry);
		assertThat(result, is(jsonNegotiator));
	}
}
