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
package com.threewks.thundr.view;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.threewks.thundr.http.StatusCode;

public class DataViewTest {

	@Test
	public void shouldRetainOutput() {
		TestView view = new TestView("view");
		assertThat(view.getOutput(), is((Object) "view"));
	}

	public void shouldRetainOutputAndSettingsFromOtherDataView() {
		TestView other = new TestView("data").withHeader("header", "value").withContentType("content/type").withStatusCode(StatusCode.Accepted);
		TestView view = new TestView(other);
		assertThat(view.getOutput(), is((Object) "data"));
		assertThat(view.getContentType(), is("content/type"));
		assertThat(view.getStatusCode(), is(StatusCode.Accepted));
		assertThat(view.getHeader("header"), is("value"));
	}

	private static class TestView extends DataView<TestView> {
		public TestView(DataView<?> other) {
			super(other);
		}

		public TestView(Object output) {
			super(output);
		}
	}
}
