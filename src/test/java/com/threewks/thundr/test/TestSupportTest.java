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
package com.threewks.thundr.test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TestSupportTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void shouldSetField() {
		TestClass testClass = new TestClass();
		assertThat(testClass.getField(), is(nullValue()));

		TestSupport.setField(testClass, "field", "value");

		assertThat(testClass.getField(), is("value"));
	}

	@Test
	public void shouldGetField() {
		TestClass testClass = new TestClass();
		assertThat(TestSupport.getField(testClass, "field"), is(nullValue()));
		testClass.setField("value");

		assertThat(TestSupport.getField(testClass, "field"), is((Object) "value"));
	}

	@Test
	public void shouldDoNothingWhenTargetIsNull() {
		TestSupport.setField(null, "field", "value");
	}

	@Test
	public void shouldThrowNPEWhenWhenFieldNotPresent() {
		thrown.expect(NullPointerException.class);
		assertThat(TestSupport.getField(new TestClass(), "field2"), is(nullValue()));
	}

	@Test
	public void shouldThrowNPEWhenTargetIsNull() {
		thrown.expect(NullPointerException.class);
		assertThat(TestSupport.getField(null, "field"), is(nullValue()));
	}

	@Test
	public void shouldThrowIllegalArgumentExceptionWhenTypesDontMatch() {
		thrown.expect(IllegalArgumentException.class);

		TestSupport.setField(new TestClass(), "field", 1l);

	}

	private static class TestClass {
		private String field;

		public String getField() {
			return field;
		}

		public void setField(String field) {
			this.field = field;
		}
	}
}
