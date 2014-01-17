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
package com.threewks.thundr.module;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.threewks.thundr.injection.InjectionContextImpl;
import com.threewks.thundr.injection.UpdatableInjectionContext;
import com.threewks.thundr.module.test.m1.M1Module;
import com.threewks.thundr.module.test.m2.M2Module;

public class ModulesModuleTest {

	@Rule public ExpectedException thrown = ExpectedException.none();

	private UpdatableInjectionContext injectionContext = new InjectionContextImpl();
	private ModulesModule config = new ModulesModule();
	private Modules modules = new Modules();

	@Before
	public void before() {
		injectionContext.inject(modules).as(Modules.class);
	}

	@Test
	public void shouldLoadApplicationModuleAsNamedInProperty() {
		injectionContext.inject("com.threewks.thundr.module.test.m1.M1Module").named(ModulesModule.ApplicationClassProperty).as(String.class);

		config.initialise(injectionContext);
		assertThat(modules.listModules().size(), is(1));
		assertThat(modules.hasModule(M1Module.class), is(true));
	}

	@Test
	public void shouldLoadNamedApplicationModule() {
		injectionContext.inject("com.threewks.thundr.module.test.m2.M2Module").named(ModulesModule.ApplicationClassProperty).as(String.class);
		assertThat((Object) config.loadApplicationModule(injectionContext), is((Object) M2Module.class));
	}
	
	@Test
	public void shouldThrowAnExceptionIfClassDoesntExist() {
		thrown.expect(ModuleLoadingException.class);
		thrown.expectMessage("Failed to load the application module. You must provide an implementation of 'com.threewks.thundr.injection.Module', either as 'ApplicationModule' in the default package, or you can specify the full class name in the configuration property 'applicationModule'.");
		
		injectionContext.inject("com.threewks.thundr.module.NotAClass").named(ModulesModule.ApplicationClassProperty).as(String.class);
		config.initialise(injectionContext);
	}
	
	@Test
	public void shouldThrowAnExceptionIfClassIsNotaModule() {
		thrown.expect(ModuleLoadingException.class);
		thrown.expectMessage("Failed to load the application module. 'com.threewks.thundr.module.ModulesModuleTest' does not implement 'com.threewks.thundr.injection.Module'");

		injectionContext.inject(this.getClass().getName()).named(ModulesModule.ApplicationClassProperty).as(String.class);
		config.initialise(injectionContext);
	}

	@Test
	public void shouldThrowAnExceptionIfNoModulesConfigured() {
		thrown.expect(ModuleLoadingException.class);
		thrown.expectMessage("Failed to load the application module. You must provide an implementation of 'com.threewks.thundr.injection.Module', either as 'ApplicationModule' in the default package, or you can specify the full class name in the configuration property 'applicationModule'.");
		config.initialise(injectionContext);
	}

}
