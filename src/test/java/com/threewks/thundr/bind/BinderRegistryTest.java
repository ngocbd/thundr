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
package com.threewks.thundr.bind;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import com.threewks.thundr.bind.http.HttpBinder;
import com.threewks.thundr.bind.parameter.ParameterBinderRegistry;
import com.threewks.thundr.transformer.TransformerManager;

public class BinderRegistryTest {

	private ParameterBinderRegistry parameterBinderRegistry;
	private TransformerManager transformerManager;

	@Before
	public void before() {
		transformerManager = TransformerManager.createWithDefaults();
		parameterBinderRegistry = new ParameterBinderRegistry(transformerManager);
		ParameterBinderRegistry.addDefaultBinders(parameterBinderRegistry);
	}

	@Test
	public void shouldAllowRegistrationOfBinder() {

		BinderRegistry binderRegistry = new BinderRegistry();
		Iterator<Binder> iterator = binderRegistry.list().iterator();
		assertThat(iterator.hasNext(), is(false));
		assertThat(binderRegistry.contains(HttpBinder.class), is(false));

		binderRegistry.add(new HttpBinder(parameterBinderRegistry));

		assertThat(binderRegistry.contains(HttpBinder.class), is(true));

		iterator = binderRegistry.list().iterator();
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next() instanceof HttpBinder, is(true));
		assertThat(iterator.hasNext(), is(false));
	}

	@Test
	public void shouldAllowDeregistrationOfBinder() {
		BinderRegistry binderRegistry = new BinderRegistry();

		binderRegistry.add(new HttpBinder(parameterBinderRegistry));
		Iterator<Binder> iterator = binderRegistry.list().iterator();
		assertThat(iterator.next() instanceof HttpBinder, is(true));

		assertThat(binderRegistry.contains(HttpBinder.class), is(true));

		binderRegistry.remove(HttpBinder.class);

		assertThat(binderRegistry.contains(HttpBinder.class), is(false));
		iterator = binderRegistry.list().iterator();
		assertThat(iterator.hasNext(), is(false));
	}
}
