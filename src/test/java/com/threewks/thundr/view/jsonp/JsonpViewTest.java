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
package com.threewks.thundr.view.jsonp;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.threewks.thundr.http.StatusCode;
import com.threewks.thundr.view.DataView;
import com.threewks.thundr.view.negotiating.NegotiatingView;

public class JsonpViewTest {
	@Test
	public void shouldRetainContentAndDefaultContentTypeAndCharacterEncodingAndStatus() {
		JsonpView view = new JsonpView("string");
		assertThat(view.getOutput(), is((Object) "string"));
		assertThat(view.getContentType(), is("application/javascript"));
		assertThat(view.getCharacterEncoding(), is("UTF-8"));
		assertThat(view.getStatusCode(), is(StatusCode.OK));
	}

	@Test
	public void shouldBeAbleToSetExtendedValuesDirectly() {
		JsonpView view = new JsonpView("view");
		assertThat(view.getContentType(), is("application/javascript"));
		assertThat(view.getCharacterEncoding(), is("UTF-8"));
		assertThat(view.getHeader("header"), is(nullValue()));
		assertThat(view.getCookie("cookie"), is(nullValue()));

		view.withContentType("content/type").withCharacterEncoding("UTF-16").withHeader("header", "value1").withCookie("cookie", "value2");

		assertThat(view.getContentType(), is("content/type"));
		assertThat(view.getCharacterEncoding(), is("UTF-16"));
		assertThat(view.getHeader("header"), is((Object)"value1"));
		assertThat(view.getCookie("cookie"), is(notNullValue()));
	}
	

	@Test
	public void shouldAllowCreationFromADataViewDefaultingContentTypeAndCharacterEncodingAndStatus() {
		DataView<?> dataView = new NegotiatingView("Test output");
		assertThat(dataView.getContentType(), is(nullValue()));
		assertThat(dataView.getCharacterEncoding(), is(nullValue()));
		assertThat(dataView.getStatusCode(), is(nullValue()));

		JsonpView view = new JsonpView(dataView);
		assertThat(view.getOutput(), is((Object) "Test output"));
		assertThat(view.getContentType(), is("application/javascript"));
		assertThat(view.getCharacterEncoding(), is("UTF-8"));
		assertThat(view.getStatusCode(), is(StatusCode.OK));
	}

	@Test
	public void shouldAllowCreationFromADataViewRespectingExistingContentTypeAndCharacterEncoding() {
		DataView<?> dataView = new NegotiatingView("Test output").withStatusCode(StatusCode.BadRequest).withCharacterEncoding("UTF-7").withContentType("text/plain");

		JsonpView view = new JsonpView(dataView);
		assertThat(view.getOutput(), is((Object) "Test output"));
		assertThat(view.getContentType(), is("text/plain"));
		assertThat(view.getCharacterEncoding(), is("UTF-7"));
		assertThat(view.getStatusCode(), is(StatusCode.BadRequest));
	}
}
