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
package com.threewks.thundr.request;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.threewks.thundr.injection.InjectionContextImpl;
import com.threewks.thundr.injection.UpdatableInjectionContext;

public class RequestModuleTest {
	private RequestModule module = new RequestModule();
	private UpdatableInjectionContext injectionContext = new InjectionContextImpl();

	@Test
	public void shouldProvideARequestContainerAtInitialise() {
		module.initialise(injectionContext);

		assertThat(injectionContext.contains(RequestContainer.class), is(true));
		assertThat(injectionContext.contains(MutableRequestContainer.class), is(true));

		RequestContainer requestContainer = injectionContext.get(RequestContainer.class);
		assertThat(requestContainer instanceof ThreadLocalRequestContainer, is(true));
	}
}
