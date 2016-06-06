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
package com.threewks.thundr.view.negotiating;

import static com.atomicleopard.expressive.Expressive.list;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.atomicleopard.expressive.Cast;
import com.google.gson.GsonBuilder;
import com.threewks.thundr.http.Header;
import com.threewks.thundr.http.StatusCode;
import com.threewks.thundr.json.GsonSupport;
import com.threewks.thundr.request.Request;
import com.threewks.thundr.request.mock.MockRequest;
import com.threewks.thundr.request.mock.MockResponse;
import com.threewks.thundr.test.TestSupport;
import com.threewks.thundr.view.ViewResolverRegistry;
import com.threewks.thundr.view.json.JsonNegotiator;
import com.threewks.thundr.view.json.JsonView;
import com.threewks.thundr.view.json.JsonViewResolver;
import com.threewks.thundr.view.jsonp.JsonpNegotiator;
import com.threewks.thundr.view.jsonp.JsonpView;
import com.threewks.thundr.view.jsonp.JsonpViewResolver;
import com.threewks.thundr.view.negotiating.strategy.AcceptsHeaderNegotiationStrategy;
import com.threewks.thundr.view.negotiating.strategy.ContentTypeNegotiationStrategy;
import com.threewks.thundr.view.negotiating.strategy.DefaultNegotiatorNegotiationStrategy;
import com.threewks.thundr.view.negotiating.strategy.FileExtensionNegotiationStrategy;
import com.threewks.thundr.view.negotiating.strategy.NegotiationStrategy;

public class NegotiatingViewResolverTest {
	private static final GsonBuilder GsonBuilder = GsonSupport.createBasicGsonBuilder();
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	private ViewResolverRegistry viewResolverRegistry = new ViewResolverRegistry();
	private ViewNegotiatorRegistry viewNegotiatorRegistry = new ViewNegotiatorRegistryImpl();
	private NegotiatingViewResolver viewResolver = new NegotiatingViewResolver(viewResolverRegistry, viewNegotiatorRegistry);
	private MockRequest req = new MockRequest();
	private MockResponse resp = new MockResponse();

	@Test
	public void shouldRetainViewResolverRegistry() {
		ViewResolverRegistry containedViewResolverRegistry = TestSupport.getField(viewResolver, "viewResolverRegistry");
		assertThat(containedViewResolverRegistry, is(viewResolverRegistry));
	}

	@Test
	public void shouldReturnNegotiatedView() {
		viewNegotiatorRegistry.addNegotiator("application/json", new JsonNegotiator());

		Request req = new MockRequest().withHeader(Header.Accept, "application/json");
		Object payload = "Test";
		Object view = viewResolver.determineView(req, new NegotiatingView(payload));
		assertThat(view instanceof JsonView, is(true));
		JsonView jsonView = Cast.as(view, JsonView.class);
		assertThat(jsonView.getOutput(), is((Object) "Test"));
	}

	@Test
	public void shouldReturnNegotiatedViewRespectingQualityParameters() {
		viewNegotiatorRegistry.addNegotiator("application/json", new JsonNegotiator());
		viewNegotiatorRegistry.addNegotiator("application/javascript", new JsonpNegotiator());

		Request req = new MockRequest().withHeader(Header.Accept, "application/json;q=0.7,application/javascript;q=0.8");
		Object payload = "Test";
		Object view = viewResolver.determineView(req, new NegotiatingView(payload));
		assertThat(view instanceof JsonpView, is(true));
		JsonpView jsonpView = Cast.as(view, JsonpView.class);
		assertThat(jsonpView.getOutput(), is((Object) "Test"));
	}

	@Test
	public void shouldReturnNegotiatedViewAssumingNoQualityIsEquivalentToOne() {
		viewNegotiatorRegistry.addNegotiator("application/json", new JsonNegotiator());
		viewNegotiatorRegistry.addNegotiator("application/javascript", new JsonpNegotiator());

		Request req = new MockRequest().withHeader(Header.Accept, "application/json;q=0.9,application/javascript");
		Object payload = "Test";
		Object view = viewResolver.determineView(req, new NegotiatingView(payload));
		assertThat(view instanceof JsonpView, is(true));
		JsonpView jsonpView = Cast.as(view, JsonpView.class);
		assertThat(jsonpView.getOutput(), is((Object) "Test"));
	}

	@Test
	public void shouldReturnNegotiatedViewIgnoringPreferredTypesThatArentSupported() {
		viewNegotiatorRegistry.addNegotiator("application/json", new JsonNegotiator());
		viewNegotiatorRegistry.addNegotiator("application/javascript", new JsonpNegotiator());

		Request req = new MockRequest().withHeader(Header.Accept, "text/plain;q=1,application/json;q=0.9");

		Object view = viewResolver.determineView(req, new NegotiatingView("Test"));

		assertThat(view instanceof JsonView, is(true));
		JsonView jsonView = Cast.as(view, JsonView.class);
		assertThat(jsonView.getOutput(), is((Object) "Test"));
	}

	@Test
	public void shouldRespondNotAcceptableWhenNoViewCanBeNegotiated() throws IOException {
		req.withHeader(Header.Accept, "text/plain;q=1,application/json;q=0.9");
		MockResponse resp = new MockResponse();

		viewResolver.resolve(req, resp, new NegotiatingView("Test"));

		assertThat(resp.getStatusCode(), is(StatusCode.NotAcceptable));
		assertThat(resp.getStatusMessage(), is("Unable to match any requested content types in the Accept header."));
	}

	@Test
	public void shouldRespondDefaultNegotiatedViewWhenNoViewCanBeNegotiated() {
		viewResolverRegistry.addResolver(JsonView.class, new JsonViewResolver(GsonBuilder));
		viewNegotiatorRegistry.setDefaultNegotiator(new JsonNegotiator());

		req.withHeader(Header.Accept, "text/plain;q=1,application/javascript;q=0.9");

		viewResolver.resolve(req, resp, new NegotiatingView("Test"));

		assertThat(resp.getBodyAsString(), is("\"Test\""));
		assertThat(resp.getStatusCode(), is(StatusCode.OK));
	}

	@Test
	public void shouldRespectAContentTypeSetOnTheView() {
		viewResolverRegistry.addResolver(JsonView.class, new JsonViewResolver(GsonBuilder));
		viewResolverRegistry.addResolver(JsonpView.class, new JsonpViewResolver(GsonBuilder));

		viewNegotiatorRegistry.addNegotiator("application/json", new JsonNegotiator());
		viewNegotiatorRegistry.addNegotiator("application/javascript", new JsonpNegotiator());

		req.withHeader(Header.Accept, "application/json");

		viewResolver.resolve(req, resp, new NegotiatingView("Test").withContentType("application/javascript"));

		assertThat(resp.getBodyAsString(), is("callback(\"Test\");"));
		assertThat(resp.getStatusCode(), is(StatusCode.OK));
	}

	@Test
	public void shouldRespectAcceptHeaderIfContentTypeCannotBeUsed() {
		viewResolverRegistry.addResolver(JsonView.class, new JsonViewResolver(GsonBuilder));
		viewResolverRegistry.addResolver(JsonpView.class, new JsonpViewResolver(GsonBuilder));

		viewNegotiatorRegistry.addNegotiator("application/json", new JsonNegotiator());
		viewNegotiatorRegistry.addNegotiator("application/javascript", new JsonpNegotiator());

		req.withHeader(Header.Accept, "application/json");

		viewResolver.resolve(req, resp, new NegotiatingView("Test").withContentType("no/registered"));

		assertThat(resp.getBodyAsString(), is("\"Test\""));
		assertThat(resp.getStatusCode(), is(StatusCode.OK));
	}

	@Test
	public void shouldRespondWithNegotiatedView() {
		viewResolverRegistry.addResolver(JsonView.class, new JsonViewResolver(GsonBuilder));
		viewNegotiatorRegistry.addNegotiator("application/javascript", new JsonNegotiator());

		req.withHeader(Header.Accept, "text/plain;q=1,application/javascript;q=0.9");

		viewResolver.resolve(req, resp, new NegotiatingView("Test"));

		assertThat(resp.getBodyAsString(), is("\"Test\""));
		assertThat(resp.getStatusCode(), is(StatusCode.OK));
	}

	@Test
	public void shouldAllowAdditionOfNegotiationStrategies() {
		NegotiationStrategy negotiationStrategy = mock(NegotiationStrategy.class);
		viewResolver.addNegotiationStrategy(negotiationStrategy);
		assertThat(viewResolver.getNegotiationStrategies(), hasItem(negotiationStrategy));
	}

	@Test
	public void shouldAddAdditionalNegotationStrategiesToTheTopOfTheList() {
		NegotiationStrategy negotiationStrategy = mock(NegotiationStrategy.class);
		viewResolver.addNegotiationStrategy(negotiationStrategy);

		List<NegotiationStrategy> negotiationStrategies = viewResolver.getNegotiationStrategies();

		assertThat(negotiationStrategies.get(0), is(negotiationStrategy));
		assertThat(negotiationStrategies.get(1) instanceof ContentTypeNegotiationStrategy, is(true));
		assertThat(negotiationStrategies.get(2) instanceof FileExtensionNegotiationStrategy, is(true));
		assertThat(negotiationStrategies.get(3) instanceof AcceptsHeaderNegotiationStrategy, is(true));
		assertThat(negotiationStrategies.get(4) instanceof DefaultNegotiatorNegotiationStrategy, is(true));
	}

	@Test
	public void shouldAllowRemovalOfNegotiationStrategies() {
		NegotiationStrategy negotiationStrategy = mock(NegotiationStrategy.class);
		viewResolver.addNegotiationStrategy(negotiationStrategy);
		assertThat(viewResolver.getNegotiationStrategies(), hasItem(negotiationStrategy));

		viewResolver.removeNegotiationStrategy(DefaultNegotiatorNegotiationStrategy.class);
		viewResolver.removeNegotiationStrategy(ContentTypeNegotiationStrategy.class);
		viewResolver.removeNegotiationStrategy(FileExtensionNegotiationStrategy.class);
		viewResolver.removeNegotiationStrategy(AcceptsHeaderNegotiationStrategy.class);

		assertThat(list(viewResolver.getNegotiationStrategies()).size(), is(1));
		assertThat(viewResolver.getNegotiationStrategies(), hasItem(negotiationStrategy));
	}

	@Test
	public void shouldRegisterDefaultNegotiationStrategies() {
		List<NegotiationStrategy> negotiationStrategies = viewResolver.getNegotiationStrategies();
		assertThat(negotiationStrategies.get(0) instanceof ContentTypeNegotiationStrategy, is(true));
		assertThat(negotiationStrategies.get(1) instanceof FileExtensionNegotiationStrategy, is(true));
		assertThat(negotiationStrategies.get(2) instanceof AcceptsHeaderNegotiationStrategy, is(true));
		assertThat(negotiationStrategies.get(3) instanceof DefaultNegotiatorNegotiationStrategy, is(true));

	}
}
