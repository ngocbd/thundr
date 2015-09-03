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
package com.threewks.thundr.bind.http;

import static com.atomicleopard.expressive.Expressive.map;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.junit.Before;
import org.junit.Test;

import com.threewks.thundr.bind.parameter.ParameterBinderRegistry;
import com.threewks.thundr.http.ContentType;
import com.threewks.thundr.introspection.ParameterDescription;
import com.threewks.thundr.request.mock.MockRequest;
import com.threewks.thundr.request.mock.MockResponse;
import com.threewks.thundr.route.HttpMethod;
import com.threewks.thundr.transformer.TransformerManager;

public class HttpBinderTest {
	private HttpBinder binder;
	private MockRequest request = new MockRequest(HttpMethod.GET, "/path");
	private MockResponse response = new MockResponse(TransformerManager.createWithDefaults());
	private Map<ParameterDescription, Object> parameterDescriptions;
	private ParameterBinderRegistry parameterBinderRegistry;

	@Before
	public void before() {
		parameterBinderRegistry = new ParameterBinderRegistry(TransformerManager.createWithDefaults());
		ParameterBinderRegistry.addDefaultBinders(parameterBinderRegistry);
		binder = new HttpBinder(parameterBinderRegistry);

		parameterDescriptions = new LinkedHashMap<ParameterDescription, Object>();
	}

	@Test
	public void shouldBindNullContentType() {
		request.withContentType(ContentType.Null);
		request.withParameter("param1", "1");

		ParameterDescription param1 = new ParameterDescription("param1", int.class);

		request.withContentTypeString(null);
		parameterDescriptions = map(param1, null);
		binder.bindAll(parameterDescriptions, request, response);
		assertThat(parameterDescriptions.get(param1), is((Object) 1));
	}

	@Test
	public void shouldBindAnyContentType() {
		request.withContentType(ContentType.Null);
		request.withParameter("param1", "1");

		ParameterDescription param1 = new ParameterDescription("param1", int.class);

		for (ContentType contentType : ContentType.values()) {
			request.withContentType(contentType);
			parameterDescriptions = map(param1, null);
			binder.bindAll(parameterDescriptions, request, response);
			assertThat(parameterDescriptions.get(param1), is((Object) 1));
		}
	}

	@Test
	public void shouldBindMultipleParams() {
		request.withContentType(ContentType.Null);
		request.withParameter("param1", "1");
		request.withParameter("param2", "2");
		request.withParameter("param3.value1", "3");
		request.withParameter("param3.value2", "three");

		ParameterDescription param1 = new ParameterDescription("param1", int.class);
		ParameterDescription param2 = new ParameterDescription("param2", String.class);
		ParameterDescription param3 = new ParameterDescription("param3", TestBean.class);
		parameterDescriptions.put(param1, null);
		parameterDescriptions.put(param2, null);
		parameterDescriptions.put(param3, null);

		binder.bindAll(parameterDescriptions, request, response);
		assertThat(parameterDescriptions.get(param1), is((Object) 1));
		assertThat(parameterDescriptions.get(param2), is((Object) "2"));
		assertThat(parameterDescriptions.get(param3), is((Object) new TestBean("3", "three")));
	}

	public static class TestBean {
		private String value1;
		private String value2;

		public TestBean() {

		}

		public TestBean(String value1, String value2) {
			this.value1 = value1;
			this.value2 = value2;
		}

		public String getValue1() {
			return value1;
		}

		public void setValue1(String value1) {
			this.value1 = value1;
		}

		public String getValue2() {
			return value2;
		}

		public void setValue2(String value2) {
			this.value2 = value2;
		}

		@Override
		public int hashCode() {
			return HashCodeBuilder.reflectionHashCode(this, false);
		}

		@Override
		public boolean equals(Object obj) {
			return EqualsBuilder.reflectionEquals(this, obj, false);
		}
	}
}
