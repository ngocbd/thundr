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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.util.Iterator;

import org.hamcrest.Matchers;
import org.junit.Test;

import com.threewks.thundr.injection.Module;
import com.threewks.thundr.module.test.TestModule;
import com.threewks.thundr.module.test.m1.M1Module;
import com.threewks.thundr.module.test.m2.M2Module;

public class DependencyRegistryTest {

	@Test
	public void shouldRetainRegisteredDependenciesInOrder() {
		DependencyRegistry dependencyRegistry = new DependencyRegistry();
		dependencyRegistry.addDependency(M1Module.class);
		dependencyRegistry.addDependency(M2Module.class);
		dependencyRegistry.addDependency(TestModule.class);

		assertThat(dependencyRegistry.hasDependency(M1Module.class), is(true));
		assertThat(dependencyRegistry.hasDependency(M2Module.class), is(true));
		assertThat(dependencyRegistry.hasDependency(TestModule.class), is(true));

		assertThat(dependencyRegistry.getDependencies().isEmpty(), is(false));
		assertThat(dependencyRegistry.getDependencies().size(), is(3));
		Iterator<Class<? extends Module>> iterator = dependencyRegistry.getDependencies().iterator();
		assertThat(iterator.next(), Matchers.<Class<? extends Module>> is(M1Module.class));
		assertThat(iterator.next(), Matchers.<Class<? extends Module>> is(M2Module.class));
		assertThat(iterator.next(), Matchers.<Class<? extends Module>> is(TestModule.class));
	}

	@Test
	public void shouldReturnEmptyCollectionWhenNothingRegistered() {
		assertThat(new DependencyRegistry().getDependencies(), is(notNullValue()));
		assertThat(new DependencyRegistry().getDependencies().isEmpty(), is(true));
	}

	@Test
	public void shouldOnlyRegisterEachDependencyOnce() {
		DependencyRegistry dependencyRegistry = new DependencyRegistry();
		dependencyRegistry.addDependency(M1Module.class);
		dependencyRegistry.addDependency(M1Module.class);
		dependencyRegistry.addDependency(M1Module.class);

		assertThat(dependencyRegistry.hasDependency(M1Module.class), is(true));

		assertThat(dependencyRegistry.getDependencies().isEmpty(), is(false));
		assertThat(dependencyRegistry.getDependencies().size(), is(1));
	}
}
