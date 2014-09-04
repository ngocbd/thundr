/*
 * This file is a component of thundr, a software library from 3wks.
 * Read more: http://www.3wks.com.au/thundr
 * Copyright (C) 2013 3wks, <thundr@3wks.com.au>
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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import com.threewks.thundr.bind.parameter.BasicTypesParameterBinder;
import com.threewks.thundr.bind.parameter.ParameterBinderRegistry;
import com.threewks.thundr.bind.parameter.RequestDataMap;
import com.threewks.thundr.introspection.ParameterDescription;
import com.threewks.thundr.transformer.TransformerManager;

public class BasicTypesParameterBinderTest {
	private BasicTypesParameterBinder binder;
	private ParameterBinderRegistry binders;
	private TransformerManager transformerManager;

	@Before
	public void before() {
		transformerManager = TransformerManager.createWithDefaults();
		binder = new BasicTypesParameterBinder();
		binders = null;
	}
	@Test
	public void shouldReturnTrueForWillBindBasicTypes() {
		assertThat(binder.willBind(description("name", byte.class), transformerManager), is(true));
		assertThat(binder.willBind(description("name", Byte.class), transformerManager), is(true));
		assertThat(binder.willBind(description("name", boolean.class), transformerManager), is(true));
		assertThat(binder.willBind(description("name", Boolean.class), transformerManager), is(true));
		assertThat(binder.willBind(description("name", float.class), transformerManager), is(true));
		assertThat(binder.willBind(description("name", Float.class), transformerManager), is(true));
		assertThat(binder.willBind(description("name", double.class), transformerManager), is(true));
		assertThat(binder.willBind(description("name", Double.class), transformerManager), is(true));
		assertThat(binder.willBind(description("name", int.class), transformerManager), is(true));
		assertThat(binder.willBind(description("name", Integer.class), transformerManager), is(true));
		assertThat(binder.willBind(description("name", long.class), transformerManager), is(true));
		assertThat(binder.willBind(description("name", Long.class), transformerManager), is(true));
		assertThat(binder.willBind(description("name", char.class), transformerManager), is(true));
		assertThat(binder.willBind(description("name", Character.class), transformerManager), is(true));
		assertThat(binder.willBind(description("name", String.class), transformerManager), is(true));
		assertThat(binder.willBind(description("name", Date.class), transformerManager), is(true));
		assertThat(binder.willBind(description("name", DateTime.class), transformerManager), is(true));
		assertThat(binder.willBind(description("name", UUID.class), transformerManager), is(true));
		assertThat(binder.willBind(description("name", BigInteger.class), transformerManager), is(true));
		assertThat(binder.willBind(description("name", BigDecimal.class), transformerManager), is(true));
		assertThat(binder.willBind(description("name", URI.class), transformerManager), is(true));
		assertThat(binder.willBind(description("name", URL.class), transformerManager), is(true));
	}

	@SuppressWarnings("deprecation")
	@Test
	public void shouldBindTypes() throws MalformedURLException, URISyntaxException {
		assertThat(binder.bind(binders, description("name", byte.class), data("name", "1"), transformerManager), is((Object) (byte) 1));
		assertThat(binder.bind(binders, description("name", Byte.class), data("name", "1"), transformerManager), is((Object) (byte) 1));
		assertThat(binder.bind(binders, description("name", boolean.class), data("name", "true"), transformerManager), is((Object) true));
		assertThat(binder.bind(binders, description("name", Boolean.class), data("name", "true"), transformerManager), is((Object) true));
		assertThat(binder.bind(binders, description("name", float.class), data("name", "1.1"), transformerManager), is((Object) 1.1f));
		assertThat(binder.bind(binders, description("name", Float.class), data("name", "1.1"), transformerManager), is((Object) 1.1f));
		assertThat(binder.bind(binders, description("name", double.class), data("name", "1.1"), transformerManager), is((Object) 1.1));
		assertThat(binder.bind(binders, description("name", Double.class), data("name", "1.1"), transformerManager), is((Object) 1.1));
		assertThat(binder.bind(binders, description("name", int.class), data("name", "1"), transformerManager), is((Object) 1));
		assertThat(binder.bind(binders, description("name", Integer.class), data("name", "1"), transformerManager), is((Object) 1));
		assertThat(binder.bind(binders, description("name", long.class), data("name", "1"), transformerManager), is((Object) 1l));
		assertThat(binder.bind(binders, description("name", Long.class), data("name", "1"), transformerManager), is((Object) 1l));
		assertThat(binder.bind(binders, description("name", char.class), data("name", "1"), transformerManager), is((Object) '1'));
		assertThat(binder.bind(binders, description("name", Character.class), data("name", "1"), transformerManager), is((Object) '1'));
		assertThat(binder.bind(binders, description("name", String.class), data("name", "1"), transformerManager), is((Object) "1"));
		assertThat(binder.bind(binders, description("name", Date.class), data("name", "2014-10-10"), transformerManager), is((Object) new Date(114, 9, 10)));
		assertThat(binder.bind(binders, description("name", DateTime.class), data("name", "2014-10-09"), transformerManager), is((Object) new DateTime(2014, 10, 9, 0, 0)));
		assertThat(binder.bind(binders, description("name", DateTime.class), data("name", "2014-10-09T01:02:03.123"), transformerManager), is((Object) new DateTime(2014, 10, 9, 1, 2, 3, 123)));
		assertThat(binder.bind(binders, description("name", UUID.class), data("name", UUID.randomUUID().toString()), transformerManager), is(notNullValue()));
		assertThat(binder.bind(binders, description("name", BigInteger.class), data("name", "1"), transformerManager), is((Object) BigInteger.valueOf(1)));
		assertThat(binder.bind(binders, description("name", BigDecimal.class), data("name", "1.1"), transformerManager), is((Object) new BigDecimal("1.1")));
		assertThat(binder.bind(binders, description("name", URL.class), data("name", "http://www.google.com/"), transformerManager), is((Object) new URL("http://www.google.com/")));
		assertThat(binder.bind(binders, description("name", URI.class), data("name", "http://www.google.com/"), transformerManager), is((Object) new URI("http://www.google.com/")));
	}

	private ParameterDescription description(String name, Class<?> type) {
		return new ParameterDescription(name, type);
	}

	private RequestDataMap data(String key, String value) {
		return new RequestDataMap(Collections.singletonMap(key, new String[] { value }));
	}
}
