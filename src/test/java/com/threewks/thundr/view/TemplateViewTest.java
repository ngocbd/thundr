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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class TemplateViewTest {

	@Test
	public void shouldRetainTemplateAndCreateEmptyModel() {
		TestView view = new TestView("view");
		assertThat(view.getView(), is("view"));
		assertThat(view.getModel(), is(notNullValue()));
		assertThat(view.getModel().isEmpty(), is(true));
	}

	@Test
	public void shouldRetainTemplateAndModel() {
		Map<String, Object> model = new HashMap<>();
		TestView view = new TestView("view", model);
		assertThat(view.getView(), is("view"));
		assertThat(view.getModel(), sameInstance(model));
	}

	private static class TestView extends TemplateView<TestView> {

		public TestView(String view) {
			super(view);
		}

		public TestView(String view, Map<String, Object> model) {
			super(view, model);
		}
	}
}
