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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.atomicleopard.expressive.Expressive;
import com.threewks.thundr.bind.parameter.EnumParameterBinder;
import com.threewks.thundr.bind.parameter.RequestDataMap;
import com.threewks.thundr.introspection.ParameterDescription;
import com.threewks.thundr.transformer.TransformerManager;

public class EnumParameterBinderTest {
	private EnumParameterBinder binder = new EnumParameterBinder();
	private TransformerManager transformerManager = TransformerManager.createWithDefaults();

	@Test
	public void shouldReturnTrueForWillBindOnAllEnumTypes() {
		assertThat(binder.willBind(new ParameterDescription("name", TestEnum.class), transformerManager), is(true));
		assertThat(binder.willBind(new ParameterDescription("name", RetentionPolicy.class), transformerManager), is(true));
		assertThat(binder.willBind(new ParameterDescription("name", String.class), transformerManager), is(false));
		assertThat(binder.willBind(new ParameterDescription("name", Object.class), transformerManager), is(false));
	}

	@Test
	public void shouldBindEnumsFromStrings() {
		ParameterDescription parameterDescription = new ParameterDescription("name", TestEnum.class);
		assertThat(binder.bind(null, parameterDescription, data("name", "text"), transformerManager), is((Object) TestEnum.text));
		assertThat(binder.bind(null, parameterDescription, data("name", "camelCase"), transformerManager), is((Object) TestEnum.camelCase));
		assertThat(binder.bind(null, parameterDescription, data("name", "numeric1"), transformerManager), is((Object) TestEnum.numeric1));
		assertThat(binder.bind(null, parameterDescription, data("name", "Standard"), transformerManager), is((Object) TestEnum.Standard));
		assertThat(binder.bind(null, parameterDescription, data("name", "ALL_CAPS"), transformerManager), is((Object) TestEnum.ALL_CAPS));

		assertThat(binder.bind(null, parameterDescription, data("name", "junk"), transformerManager), is(nullValue()));
		assertThat(binder.bind(null, parameterDescription, data("name", "all_caps"), transformerManager), is(nullValue()));
		assertThat(binder.bind(null, parameterDescription, data("name", "CAMELCASE"), transformerManager), is(nullValue()));
		assertThat(binder.bind(null, parameterDescription, data("name", "STANDARD"), transformerManager), is(nullValue()));
	}

	private RequestDataMap data(String name, String value) {
		RequestDataMap pathMap = new RequestDataMap(Expressive.<String, List<String>> map(name, Arrays.asList(value )));
		return pathMap;
	}

	private enum TestEnum {
		text,
		camelCase,
		numeric1,
		Standard,
		ALL_CAPS;
	}
}
