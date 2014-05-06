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
package com.threewks.thundr.view.negotiating;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.threewks.thundr.view.json.JsonNegotiator;
import com.threewks.thundr.view.json.JsonView;
import com.threewks.thundr.view.jsonp.JsonpNegotiator;
import com.threewks.thundr.view.jsonp.JsonpView;
import com.threewks.thundr.view.negotiating.Negotiator;
import com.threewks.thundr.view.negotiating.ViewNegotiatorRegistryImpl;

public class ViewNegotiatorRegistryImplTest {
	ViewNegotiatorRegistryImpl registry = new ViewNegotiatorRegistryImpl();

	@Test
	public void shouldAllowRegistrationOfNegotiatorForContentType() {
		Negotiator<JsonView> negotiator = new JsonNegotiator();
		registry.addNegotiator("application/json", negotiator);
		assertThat(registry.<JsonView> getNegotiator("application/json"), is(negotiator));
	}

	@Test
	public void shouldAllowRegistrationOfNegotiatorForContentTypeIgnoringCasing() {
		Negotiator<JsonView> negotiator = new JsonNegotiator();
		registry.addNegotiator("applICAtion/JSON", negotiator);
		assertThat(registry.<JsonView> getNegotiator("APPLication/JSon"), is(negotiator));
	}

	@Test
	public void shouldAllowRemovalOfRegistratedNegotiatorForContentType() {
		Negotiator<JsonView> negotiator = new JsonNegotiator();
		registry.addNegotiator("application/json", negotiator);
		assertThat(registry.<JsonView> getNegotiator("application/json"), is(negotiator));
		registry.removeNegotiator("application/json");
		assertThat(registry.<JsonView> getNegotiator("application/json"), is(nullValue()));
	}

	@Test
	public void shouldAllowReplacementOfRegisteredNegotiatorForContentType() {
		Negotiator<JsonView> negotiator = new JsonNegotiator();
		registry.addNegotiator("application/json", negotiator);
		assertThat(registry.<JsonView> getNegotiator("application/json"), is(negotiator));
		Negotiator<JsonpView> negotiator2 = new JsonpNegotiator();
		registry.addNegotiator("application/json", negotiator2);
		assertThat(registry.<JsonpView> getNegotiator("application/json"), is(negotiator2));
	}

	@Test
	public void shouldListNegotiatedContentTypes() {
		assertThat(registry.listNegotiatedContentTypes().size(), is(0));
		registry.addNegotiator("text/javascript", new JsonNegotiator());
		registry.addNegotiator("text/script", new JsonNegotiator());
		registry.addNegotiator("application/javascript", new JsonpNegotiator());
		assertThat(registry.listNegotiatedContentTypes().size(), is(3));
		assertThat(registry.listNegotiatedContentTypes(), hasItems("application/javascript", "text/javascript", "text/script"));
	}

}
