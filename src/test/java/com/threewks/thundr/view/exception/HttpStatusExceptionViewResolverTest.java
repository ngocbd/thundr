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
import com.threewks.thundr.http.exception.HttpStatusException;
import com.threewks.thundr.request.Request;
import com.threewks.thundr.request.mock.MockRequest;
import com.threewks.thundr.request.mock.MockResponse;

public class HttpStatusExceptionViewResolverTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private Request req = new MockRequest();
	private MockResponse resp = new MockResponse();
	private HttpStatusException viewResult = new HttpStatusException(StatusCode.AlreadyReported, "message");
	private HttpStatusExceptionViewResolver resolver = new HttpStatusExceptionViewResolver();

	@Test
	public void shouldSendError404() {
		resolver.resolve(req, resp, viewResult);
		assertThat(resp.getStatusCode(), is(StatusCode.AlreadyReported));
	}

	/*
	 *  TODO - v3 - this comes down to how this is implemented in the error handler
	@Test
	public void shouldThrowViewResolutionExceptionIfSendingErrorFails() {
		thrown.expect(ViewResolutionException.class);
		thrown.expectMessage("Failed to send error status 101");
		MockHttpServletResponse resp = new MockHttpServletResponse() {
			@Override
			public void sendError(int sc) throws IOException {
				throw new IOException("expected");
			}
		};
		resolver.resolve(req, resp, viewResult);
	}
	*/
}
