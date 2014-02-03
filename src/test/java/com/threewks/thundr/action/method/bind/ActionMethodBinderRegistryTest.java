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
package com.threewks.thundr.action.method.bind;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Iterator;

import org.junit.Test;

import com.threewks.thundr.action.method.bind.http.HttpBinder;
import com.threewks.thundr.action.method.bind.http.MultipartHttpBinder;
import com.threewks.thundr.action.method.bind.json.GsonBinder;
import com.threewks.thundr.action.method.bind.path.PathVariableBinder;
import com.threewks.thundr.action.method.bind.request.CookieBinder;
import com.threewks.thundr.action.method.bind.request.RequestAttributeBinder;
import com.threewks.thundr.action.method.bind.request.RequestClassBinder;
import com.threewks.thundr.action.method.bind.request.RequestHeaderBinder;
import com.threewks.thundr.action.method.bind.request.SessionAttributeBinder;

public class ActionMethodBinderRegistryTest {

	@Test
	public void shouldRegisterDefaultActionMethodBindersInOrder() {
		ActionMethodBinderRegistry actionMethodBinderRegistry = new ActionMethodBinderRegistry();
		Iterator<ActionMethodBinder> iterator = actionMethodBinderRegistry.getRegisteredActionMethodBinders().iterator();
		assertThat(iterator.hasNext(), is(false));

		actionMethodBinderRegistry.registerDefaultActionMethodBinders();

		iterator = actionMethodBinderRegistry.getRegisteredActionMethodBinders().iterator();
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

		ActionMethodBinderRegistry actionMethodBinderRegistry = new ActionMethodBinderRegistry();
		Iterator<ActionMethodBinder> iterator = actionMethodBinderRegistry.getRegisteredActionMethodBinders().iterator();
		assertThat(iterator.hasNext(), is(false));

		actionMethodBinderRegistry.registerActionMethodBinder(new HttpBinder());

		iterator = actionMethodBinderRegistry.getRegisteredActionMethodBinders().iterator();
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next() instanceof HttpBinder, is(true));
		assertThat(iterator.hasNext(), is(false));
	}

	@Test
	public void shouldAllowDeregistrationOfBinder() {
		ActionMethodBinderRegistry actionMethodBinderRegistry = new ActionMethodBinderRegistry();

		actionMethodBinderRegistry.registerActionMethodBinder(new HttpBinder());
		Iterator<ActionMethodBinder> iterator = actionMethodBinderRegistry.getRegisteredActionMethodBinders().iterator();
		assertThat(iterator.next() instanceof HttpBinder, is(true));

		actionMethodBinderRegistry.deregisterActionMethodBinder(HttpBinder.class);

		iterator = actionMethodBinderRegistry.getRegisteredActionMethodBinders().iterator();
		assertThat(iterator.hasNext(), is(false));
	}
}
