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
import com.threewks.thundr.bind.http.MultipartHttpBinder;
import com.threewks.thundr.bind.http.request.CookieBinder;
import com.threewks.thundr.bind.http.request.RequestAttributeBinder;
import com.threewks.thundr.bind.http.request.RequestClassBinder;
import com.threewks.thundr.bind.http.request.RequestHeaderBinder;
import com.threewks.thundr.bind.http.request.SessionAttributeBinder;
import com.threewks.thundr.bind.json.GsonBinder;
import com.threewks.thundr.bind.parameter.ParameterBinderRegistry;
import com.threewks.thundr.bind.path.PathVariableBinder;
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
	public void shouldRegisterDefaultBindersInOrder() {
		BinderRegistry binderRegistry = new BinderRegistry();

		Iterator<Binder> iterator = binderRegistry.getRegisteredBinders().iterator();
		assertThat(iterator.hasNext(), is(false));

		BinderRegistry.registerDefaultBinders(binderRegistry, parameterBinderRegistry, transformerManager);

		iterator = binderRegistry.getRegisteredBinders().iterator();
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next() instanceof PathVariableBinder, is(true));
		assertThat(iterator.next() instanceof RequestClassBinder, is(true));
		assertThat(iterator.next() instanceof HttpBinder, is(true));
		assertThat(iterator.next() instanceof RequestAttributeBinder, is(true));
		assertThat(iterator.next() instanceof RequestHeaderBinder, is(true));
		assertThat(iterator.next() instanceof SessionAttributeBinder, is(true));
		assertThat(iterator.next() instanceof CookieBinder, is(true));
		assertThat(iterator.next() instanceof GsonBinder, is(true));
		assertThat(iterator.next() instanceof MultipartHttpBinder, is(true));
		assertThat(iterator.hasNext(), is(false));
	}

	@Test
	public void shouldAllowRegistrationOfBinder() {

		BinderRegistry binderRegistry = new BinderRegistry();
		Iterator<Binder> iterator = binderRegistry.getRegisteredBinders().iterator();
		assertThat(iterator.hasNext(), is(false));
		assertThat(binderRegistry.hasBinder(HttpBinder.class), is(false));

		binderRegistry.registerBinder(new HttpBinder(parameterBinderRegistry));

		assertThat(binderRegistry.hasBinder(HttpBinder.class), is(true));

		iterator = binderRegistry.getRegisteredBinders().iterator();
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next() instanceof HttpBinder, is(true));
		assertThat(iterator.hasNext(), is(false));
	}

	@Test
	public void shouldAllowDeregistrationOfBinder() {
		BinderRegistry binderRegistry = new BinderRegistry();

		binderRegistry.registerBinder(new HttpBinder(parameterBinderRegistry));
		Iterator<Binder> iterator = binderRegistry.getRegisteredBinders().iterator();
		assertThat(iterator.next() instanceof HttpBinder, is(true));

		assertThat(binderRegistry.hasBinder(HttpBinder.class), is(true));

		binderRegistry.deregisterBinder(HttpBinder.class);

		assertThat(binderRegistry.hasBinder(HttpBinder.class), is(false));
		iterator = binderRegistry.getRegisteredBinders().iterator();
		assertThat(iterator.hasNext(), is(false));
	}
}
