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
package com.threewks.thundr.transformer;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.atomicleopard.expressive.ETransformer;
import com.threewks.thundr.injection.InjectionContextImpl;
import com.threewks.thundr.injection.UpdatableInjectionContext;

public class TransformerModuleTest {

	@Test
	public void shouldProvideTransformerManagerToInjectionContextAtInitialise() {
		UpdatableInjectionContext injectionContext = new InjectionContextImpl();
		new TransformerModule().initialise(injectionContext);

		TransformerManager transformerManager = injectionContext.get(TransformerManager.class);
		assertThat(transformerManager, is(notNullValue()));

		// Ensure defaults are configured
		ETransformer<String, Long> transformer = transformerManager.getTransformer(String.class, Long.class);
		assertThat(transformer, is(notNullValue()));
	}

	@Test
	public void shouldNoopForRequiresConfigureStartAndStop() {
		TransformerModule module = new TransformerModule();

		module.requires(null);
		module.configure(null);
		module.start(null);
		module.stop(null);
	}
}
