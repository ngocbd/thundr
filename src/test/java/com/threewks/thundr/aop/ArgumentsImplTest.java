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
package com.threewks.thundr.aop;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;
import java.math.BigDecimal;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.atomicleopard.expressive.Expressive;
import com.threewks.thundr.introspection.ClassIntrospector;
import com.threewks.thundr.introspection.MethodIntrospector;

public class ArgumentsImplTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();
	private ClassIntrospector c = new ClassIntrospector();
	private Method method = c.getMethod(Service.class, "method1");
	private MethodIntrospector methodIntrospector = new MethodIntrospector(method);
	private ArgumentsImpl arguments = new ArgumentsImpl(methodIntrospector, new Object[] { "string-val", 1, 2l });

	@Test
	public void shouldGetArgumentByName() {
		assertThat(arguments.<String> getArgument("string"), is("string-val"));
		assertThat(arguments.<Integer> getArgument("integer"), is(1));
		assertThat(arguments.<Long> getArgument("longer"), is(2l));
		assertThat(arguments.getArgument("other"), is(nullValue()));
	}

	@Test
	public void shouldGetArgumentByType() {
		assertThat(arguments.getArgument(String.class), is("string-val"));
		assertThat(arguments.getArgument(int.class), is(1));
		assertThat(arguments.getArgument(Long.class), is(2l));
		assertThat(arguments.getArgument(BigDecimal.class), is(nullValue()));
		assertThat(arguments.getArgument(Double.class), is(nullValue()));

	}

	@Test
	public void shouldGetArgumentByNameAndType() {

		assertThat(arguments.getArgument(String.class, "string"), is("string-val"));
		assertThat(arguments.getArgument(String.class, "another"), is(nullValue()));
		assertThat(arguments.getArgument(Double.class, "string"), is(nullValue()));
		assertThat(arguments.getArgument(int.class, "integer"), is(1));
		assertThat(arguments.getArgument(Integer.class, "integer"), is(nullValue()));
		assertThat(arguments.getArgument(int.class, "notinteger"), is(nullValue()));
		assertThat(arguments.getArgument(Long.class, "longer"), is(2l));
		assertThat(arguments.getArgument(Long.class, "notlonger"), is(nullValue()));
		assertThat(arguments.getArgument(Double.class, "longer"), is(nullValue()));

	}

	@Test
	public void shouldGetArgumentsAsList() {
		assertThat(arguments.getArguments(), Matchers.<Object> contains("string-val", 1, 2l));

		arguments.getArguments().set(0, "other");

		assertThat(arguments.getArguments(), Matchers.<Object> contains("other", 1, 2l));
	}

	@Test
	public void shouldGetArgumentsAsObjectArray() {
		assertThat(arguments.toArgs(), is(Expressive.<Object> array("string-val", 1, 2l)));
	}

	@Test
	public void shouldReplaceArgumentsByName() {
		arguments.replaceArgument("string", "newString");
		assertThat(arguments.<String> getArgument("string"), is("newString"));
		arguments.replaceArgument("integer", 123);
		assertThat(arguments.<Integer> getArgument("integer"), is(123));
		arguments.replaceArgument("longer", null);
		assertThat(arguments.getArgument("longer"), is(nullValue()));
	}

	@Test
	public void shouldReplaceArgumentsByType() {
		arguments.replaceArgument(String.class, "newString");
		assertThat(arguments.getArgument(String.class), is("newString"));
		arguments.replaceArgument(int.class, 222);
		assertThat(arguments.getArgument(int.class), is(222));
		arguments.replaceArgument(Long.class, null);
		assertThat(arguments.getArgument(Long.class), is(nullValue()));
	}

	@Test
	public void shouldReplaceArgumentsByTypeAndName() {
		arguments.replaceArgument(String.class, "anotherstring", "newString");
		assertThat(arguments.getArgument(String.class), is("string-val"));
		arguments.replaceArgument(StringBuilder.class, "string", new StringBuilder("newString"));
		assertThat(arguments.getArgument(String.class), is("string-val"));

		arguments.replaceArgument(String.class, "string", "newString");
		assertThat(arguments.getArgument(String.class), is("newString"));

		arguments.replaceArgument(int.class, "inte", 222);
		assertThat(arguments.getArgument(int.class), is(1));
		arguments.replaceArgument(Long.class, "integer", 222l);
		assertThat(arguments.getArgument(int.class), is(1));

		arguments.replaceArgument(int.class, "integer", 111);
		assertThat(arguments.getArgument(int.class), is(111));

		arguments.replaceArgument(Long.class, "long", null);
		assertThat(arguments.getArgument(Long.class), is(2l));
		arguments.replaceArgument(String.class, "longer", null);
		assertThat(arguments.getArgument(Long.class), is(2l));
		arguments.replaceArgument(Long.class, "longer", null);
		assertThat(arguments.getArgument(Long.class), is(nullValue()));
	}

	@Test
	public void shouldGenerallyHandleBoxedTypes() {
		arguments.replaceArgument(int.class, 1);
		assertThat(arguments.getArgument(int.class), is(1));

		arguments.replaceArgument(Integer.class, 2);
		assertThat(arguments.getArgument(Integer.class), is(2));
		assertThat(arguments.getArgument(int.class), is(2));
		assertThat(arguments.<Integer> getArgument("integer"), is(2));

		arguments.replaceArgument(int.class, 3);
		assertThat(arguments.getArgument(Integer.class), is(3));
		assertThat(arguments.getArgument(int.class), is(3));
		assertThat(arguments.<Integer> getArgument("integer"), is(3));

		arguments.replaceArgument(long.class, 1l);
		assertThat(arguments.getArgument(long.class), is(1l));

		arguments.replaceArgument(Long.class, 2l);
		assertThat(arguments.getArgument(Long.class), is(2l));
		assertThat(arguments.getArgument(long.class), is(2l));
		assertThat(arguments.<Long> getArgument("longer"), is(2l));

		arguments.replaceArgument(long.class, 3l);
		assertThat(arguments.getArgument(Long.class), is(3l));
		assertThat(arguments.getArgument(long.class), is(3l));
		assertThat(arguments.<Long> getArgument("longer"), is(3l));
	}

	@Test
	public void shouldNotThrowClassCastExceptionWhenAttemptToReplaceValueWithBoxedValue() {
		arguments.replaceArgument(int.class, new Integer(222));
		assertThat(arguments.getArgument(int.class), is(222));
	}

	@Test
	public void shouldThrowClassCastExceptionWhenAttemptToReplaceValueWithUnmatchedType() {
		thrown.expect(ClassCastException.class);
		thrown.expectMessage("Cannot replace parameter 'java.lang.Long longer' with a java.lang.String 'string-val' - the types are not compatible");

		arguments.replaceArgument("longer", "string-val");
	}

	protected static class Service {
		public void method1(String string, int integer, Long longer) {

		}
	}
}
