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

import static com.atomicleopard.expressive.Expressive.list;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.atomicleopard.expressive.Cast;
import com.threewks.thundr.http.Header;
import com.threewks.thundr.test.TestSupport;
import com.threewks.thundr.test.mock.servlet.MockHttpServletRequest;
import com.threewks.thundr.test.mock.servlet.MockHttpServletResponse;
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
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	private ViewResolverRegistry viewResolverRegistry = new ViewResolverRegistry();
	private ViewNegotiatorRegistry viewNegotiatorRegistry = new ViewNegotiatorRegistryImpl();
	private NegotiatingViewResolver viewResolver = new NegotiatingViewResolver(viewResolverRegistry, viewNegotiatorRegistry);
	private MockHttpServletRequest req = new MockHttpServletRequest();
	private MockHttpServletResponse resp = new MockHttpServletResponse();

	@Test
	public void shouldRetainViewResolverRegistry() {
		ViewResolverRegistry containedViewResolverRegistry = TestSupport.getField(viewResolver, "viewResolverRegistry");
		assertThat(containedViewResolverRegistry, is(viewResolverRegistry));
	}

	@Test
	public void shouldReturnNegotiatedView() {
		viewNegotiatorRegistry.addNegotiator("application/json", new JsonNegotiator());

		HttpServletRequest req = new MockHttpServletRequest().header(Header.Accept, "application/json");
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

		HttpServletRequest req = new MockHttpServletRequest().header(Header.Accept, "application/json;q=0.7,application/javascript;q=0.8");
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

		HttpServletRequest req = new MockHttpServletRequest().header(Header.Accept, "application/json;q=0.9,application/javascript");
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

		HttpServletRequest req = new MockHttpServletRequest().header(Header.Accept, "text/plain;q=1,application/json;q=0.9");

		Object view = viewResolver.determineView(req, new NegotiatingView("Test"));

		assertThat(view instanceof JsonView, is(true));
		JsonView jsonView = Cast.as(view, JsonView.class);
		assertThat(jsonView.getOutput(), is((Object) "Test"));
	}

	@SuppressWarnings("deprecation")
	@Test
	public void shouldRespondNotAcceptableWhenNoViewCanBeNegotiated() throws IOException {
		req.header(Header.Accept, "text/plain;q=1,application/json;q=0.9");
		HttpServletResponse resp = mock(HttpServletResponse.class);

		viewResolver.resolve(req, resp, new NegotiatingView("Test"));

		verify(resp).setStatus(406, "Unable to match any requested content types in the Accept header.");
		verify(resp, times(0)).getOutputStream();
		verify(resp, times(0)).getWriter();
	}

	@Test
	public void shouldRespondDefaultNegotiatedViewWhenNoViewCanBeNegotiated() {
		viewResolverRegistry.addResolver(JsonView.class, new JsonViewResolver());
		viewNegotiatorRegistry.setDefaultNegotiator(new JsonNegotiator());

		req.header(Header.Accept, "text/plain;q=1,application/javascript;q=0.9");

		viewResolver.resolve(req, resp, new NegotiatingView("Test"));

		assertThat(resp.content(), is("\"Test\""));
		assertThat(resp.status(), is(200));
	}

	@Test
	public void shouldRespectAContentTypeSetOnTheView() {
		viewResolverRegistry.addResolver(JsonView.class, new JsonViewResolver());
		viewResolverRegistry.addResolver(JsonpView.class, new JsonpViewResolver());

		viewNegotiatorRegistry.addNegotiator("application/json", new JsonNegotiator());
		viewNegotiatorRegistry.addNegotiator("application/javascript", new JsonpNegotiator());

		req.header(Header.Accept, "application/json");

		viewResolver.resolve(req, resp, new NegotiatingView("Test").withContentType("application/javascript"));

		assertThat(resp.content(), is("callback(\"Test\");"));
		assertThat(resp.status(), is(200));
	}

	@Test
	public void shouldRespectAcceptHeaderIfContentTypeCannotBeUsed() {
		viewResolverRegistry.addResolver(JsonView.class, new JsonViewResolver());
		viewResolverRegistry.addResolver(JsonpView.class, new JsonpViewResolver());

		viewNegotiatorRegistry.addNegotiator("application/json", new JsonNegotiator());
		viewNegotiatorRegistry.addNegotiator("application/javascript", new JsonpNegotiator());

		req.header(Header.Accept, "application/json");

		viewResolver.resolve(req, resp, new NegotiatingView("Test").withContentType("no/registered"));

		assertThat(resp.content(), is("\"Test\""));
		assertThat(resp.status(), is(200));
	}

	@Test
	public void shouldRespondWithNegotiatedView() {
		viewResolverRegistry.addResolver(JsonView.class, new JsonViewResolver());
		viewNegotiatorRegistry.addNegotiator("application/javascript", new JsonNegotiator());

		req.header(Header.Accept, "text/plain;q=1,application/javascript;q=0.9");

		viewResolver.resolve(req, resp, new NegotiatingView("Test"));

		assertThat(resp.content(), is("\"Test\""));
		assertThat(resp.status(), is(200));
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
