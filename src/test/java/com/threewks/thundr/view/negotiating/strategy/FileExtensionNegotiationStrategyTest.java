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
package com.threewks.thundr.view.negotiating.strategy;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.threewks.thundr.request.mock.MockRequest;
import com.threewks.thundr.view.json.JsonNegotiator;
import com.threewks.thundr.view.negotiating.NegotiatingView;
import com.threewks.thundr.view.negotiating.Negotiator;
import com.threewks.thundr.view.negotiating.ViewNegotiatorRegistry;
import com.threewks.thundr.view.negotiating.ViewNegotiatorRegistryImpl;

public class FileExtensionNegotiationStrategyTest {

	private MockRequest req;
	private NegotiatingView view;
	private ViewNegotiatorRegistry viewNegotiatorRegistry;
	private FileExtensionNegotiationStrategy strategy;
	@SuppressWarnings("rawtypes")
	private Negotiator negotiator;

	@Before
	public void before() {
		req = new MockRequest();
		view = new NegotiatingView("test");
		viewNegotiatorRegistry = new ViewNegotiatorRegistryImpl();
		strategy = new FileExtensionNegotiationStrategy();
		negotiator = new JsonNegotiator();
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void shouldReturnTheNegotiatorMatchingTheFileExtension() {
		viewNegotiatorRegistry.addNegotiator("application/json", negotiator);

		req.withRequestPath("/request/url.json");
		Negotiator result = strategy.findNegotiator(req, view, viewNegotiatorRegistry);
		assertThat(result, is(negotiator));
	}

	@Test
	public void shouldReturnNullIfNoExtensions() {
		viewNegotiatorRegistry.addNegotiator("application/json", negotiator);

		req.withRequestPath("/request/url");
		Negotiator<?> result = strategy.findNegotiator(req, view, viewNegotiatorRegistry);

		assertThat(result, is(nullValue()));
	}

	@Test
	public void shouldReturnNullIfNoNegotiatorForExtension() {
		viewNegotiatorRegistry.addNegotiator("application/json", negotiator);

		req.withRequestPath("/request/url.xml");
		Negotiator<?> result = strategy.findNegotiator(req, view, viewNegotiatorRegistry);

		assertThat(result, is(nullValue()));
	}
}
