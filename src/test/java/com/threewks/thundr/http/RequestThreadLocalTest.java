/*
 * This file is a component of thundr, a software library from 3wks.
 * Read more: http://3wks.github.io/thundr/
 * Copyright (C) 2014 3wks, <thundr@3wks.com.au>
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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Test;

import com.threewks.thundr.test.mock.servlet.MockHttpServletRequest;
import com.threewks.thundr.test.mock.servlet.MockHttpServletResponse;

public class RequestThreadLocalTest {

	@After
	public void after() {
		RequestThreadLocal.clear();
	}

	@Test
	public void shouldRetainRequestAndResponse() {
		HttpServletRequest req = new MockHttpServletRequest();
		HttpServletResponse resp = new MockHttpServletResponse();
		RequestThreadLocal.set(req, resp);
		assertThat(RequestThreadLocal.getRequest(), is(req));
		assertThat(RequestThreadLocal.getResponse(), is(resp));
	}

	@Test
	public void shouldClearRequestAndResponse() {
		HttpServletRequest req = new MockHttpServletRequest();
		HttpServletResponse resp = new MockHttpServletResponse();
		RequestThreadLocal.set(req, resp);
		assertThat(RequestThreadLocal.getRequest(), is(req));
		assertThat(RequestThreadLocal.getResponse(), is(resp));
		RequestThreadLocal.clear();
		assertThat(RequestThreadLocal.getRequest(), is(nullValue()));
		assertThat(RequestThreadLocal.getResponse(), is(nullValue()));
	}
}
