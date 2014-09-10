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
package com.threewks.thundr.platform;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import org.junit.After;
import org.junit.Test;

import com.threewks.thundr.configuration.Environment;
import com.threewks.thundr.injection.InjectionContextImpl;
import com.threewks.thundr.injection.UpdatableInjectionContext;
import com.threewks.thundr.test.TestSupport;

public class DefaultPlatformModuleTest {

	private UpdatableInjectionContext injectionContext = new InjectionContextImpl();
	private DefaultPlatformModule platformModule = new DefaultPlatformModule();

	@After
	public void after() {
		Environment.set(null);
	}

	@Test
	public void shouldSetDevEnvironmentByDefault() {
		assertThat(injectionContext.get(String.class, "environment"), is(nullValue()));
		assertThat(Environment.get(), is(nullValue()));

		platformModule.initialise(injectionContext);

		assertThat(injectionContext.get(String.class, "environment"), is("dev"));
		assertThat(Environment.get(), is("dev"));
	}

	@Test
	public void shouldSetEnvironmentFromEnvironmentVariable() {
		assertThat(injectionContext.get(String.class, "environment"), is(nullValue()));
		assertThat(Environment.get(), is(nullValue()));

		Map<String, String> environmentVars = Collections.singletonMap("thundrEnvironment", "test-env");
		TestSupport.setField(platformModule, "environmentVariables", environmentVars);

		platformModule.initialise(injectionContext);

		assertThat(injectionContext.get(String.class, "environment"), is("test-env"));
		assertThat(Environment.get(), is("test-env"));
	}

	@Test
	public void shouldSetEnvironmentFromSystemProperty() {
		assertThat(injectionContext.get(String.class, "environment"), is(nullValue()));
		assertThat(Environment.get(), is(nullValue()));

		Properties properties = new Properties();
		properties.put("thundrEnvironment", "test-env");
		TestSupport.setField(platformModule, "systemProperties", properties);

		platformModule.initialise(injectionContext);

		assertThat(injectionContext.get(String.class, "environment"), is("test-env"));
		assertThat(Environment.get(), is("test-env"));
	}

	@Test
	public void shouldPreferSystemPropertyOverEnvironmentVariable() {
		Map<String, String> environmentVars = Collections.singletonMap("thundrEnvironment", "env");
		Properties properties = new Properties();
		properties.put("thundrEnvironment", "sysprop");
		TestSupport.setField(platformModule, "systemProperties", properties);
		TestSupport.setField(platformModule, "environmentVariables", environmentVars);

		platformModule.initialise(injectionContext);

		assertThat(injectionContext.get(String.class, "environment"), is("sysprop"));
		assertThat(Environment.get(), is("sysprop"));
	}

	@Test
	public void shouldDoNothingForRequiresStartConfigureAndStop() {
		platformModule.requires(null);
		platformModule.start(null);
		platformModule.configure(null);
		platformModule.stop(null);
	}
}
