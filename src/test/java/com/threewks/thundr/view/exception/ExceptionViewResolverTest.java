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
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.Test;
import org.mockito.Mockito;

import com.threewks.thundr.http.StatusCode;
import com.threewks.thundr.request.Request;
import com.threewks.thundr.request.mock.MockResponse;
import com.threewks.thundr.view.ViewResolutionException;

public class ExceptionViewResolverTest {
	private ExceptionViewResolver resolver = new ExceptionViewResolver();
	private Request req = mock(Request.class);
	private MockResponse resp = new MockResponse();

	@Test
	public void shouldReturnStatus500() throws IOException {
		Exception cause = new Exception("cause");
		Throwable viewResult = new Exception("message", cause);
		resolver.resolve(req, resp, viewResult);

		assertThat(resp.getStatusCode(), is(StatusCode.InternalServerError));
	}

	@Test
	public void shouldSwallowAnyExceptionsDuringViewResolution() throws IOException {
		resp = spy(resp);
		ViewResolutionException viewResult = mock(ViewResolutionException.class);
		doThrow(new RuntimeException("intentional")).when(resp).withStatusCode(Mockito.any(StatusCode.class));
		resolver.resolve(req, resp, viewResult);
	}
}
