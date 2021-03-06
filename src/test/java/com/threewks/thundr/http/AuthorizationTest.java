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
package com.threewks.thundr.http;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.threewks.thundr.request.mock.MockRequest;
import com.threewks.thundr.request.mock.MockResponse;

public class AuthorizationTest {
	@Test
	public void shouldCreateBasicAuthHeaderValue() {
		assertThat(Authorization.createBasicHeader("username", "password"), is("Basic dXNlcm5hbWU6cGFzc3dvcmQ="));
	}

	@Test
	public void shouldCreateBearerHeaderValue() {
		assertThat(Authorization.createBearerHeader("token"), is("Bearer token"));
	}

	@Test
	public void shouldWriteBearerHeader() {
		MockResponse resp = new MockResponse();
		Authorization.writeBearerHeader(resp, "token");
		assertThat(resp.getHeader("Authorization"), is("Bearer token"));
	}

	@Test
	public void shouldReadBearerHeader() {
		MockRequest req = new MockRequest();
		req.withHeader("Authorization", "token");

		assertThat(Authorization.readBearerHeader(req), is("token"));

	}
}
