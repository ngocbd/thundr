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
package com.threewks.thundr.view.exception;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.threewks.thundr.http.StatusCode;
import com.threewks.thundr.request.Request;
import com.threewks.thundr.request.mock.MockRequest;
import com.threewks.thundr.request.mock.MockResponse;
import com.threewks.thundr.route.RouteNotFoundException;

public class RouteNotFoundViewResolverTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private Request req = new MockRequest();
	private MockResponse resp = new MockResponse();
	private RouteNotFoundException viewResult = new RouteNotFoundException("");
	private RouteNotFoundViewResolver resolver = new RouteNotFoundViewResolver();

	@Test
	public void shouldSendError404() {
		resolver.resolve(req, resp, viewResult);
		assertThat(resp.getStatusCode(), is(StatusCode.NotFound));
	}

	/*
	 * TODO - v3 - this comes down to error handling
	 * 
	 * @Test
	 * public void shouldThrowViewResolutionExceptionIfSendingErrorFails() {
	 * thrown.expect(ViewResolutionException.class);
	 * thrown.expectMessage("Failed to send error status 404");
	 * MockHttpServletResponse resp = new MockHttpServletResponse() {
	 * 
	 * @Override
	 * public void sendError(int sc) throws IOException {
	 * throw new IOException("expected");
	 * }
	 * };
	 * resolver.resolve(req, resp, viewResult);
	 * }
	 */
}
