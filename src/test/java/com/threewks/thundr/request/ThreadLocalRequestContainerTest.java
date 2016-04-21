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
package com.threewks.thundr.request;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.threewks.thundr.request.mock.MockRequest;
import com.threewks.thundr.request.mock.MockResponse;

public class ThreadLocalRequestContainerTest {
	ThreadLocalRequestContainer container = new ThreadLocalRequestContainer();

	@Test
	public void shouldStoreGivenRequestAndResponse() {

		assertThat(container.getRequest(), is(nullValue()));
		assertThat(container.getResponse(), is(nullValue()));
		assertThat(container.getId(), is(nullValue()));

		Response resp = new MockResponse();
		Request req = new MockRequest();
		container.set(req, resp);

		assertThat(container.getRequest(), is(req));
		assertThat(container.getResponse(), is(resp));
		assertThat(container.getId(), is(req.getId()));

		container.clear();
		assertThat(container.getRequest(), is(nullValue()));
		assertThat(container.getResponse(), is(nullValue()));
		assertThat(container.getId(), is(nullValue()));
	}
}
