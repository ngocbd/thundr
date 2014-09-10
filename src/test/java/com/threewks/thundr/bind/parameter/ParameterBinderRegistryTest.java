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
package com.threewks.thundr.bind.parameter;

import static com.atomicleopard.expressive.Expressive.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.threewks.thundr.http.MultipartFile;
import com.threewks.thundr.introspection.ParameterDescription;
import com.threewks.thundr.test.TestSupport;
import com.threewks.thundr.transformer.TransformerManager;

public class ParameterBinderRegistryTest {
	private TransformerManager transformerManager = TransformerManager.createWithDefaults();
	private ParameterBinderRegistry registry = new ParameterBinderRegistry(transformerManager);

	private TestParameterBinder binder = new TestParameterBinder();
	private TestBinaryParameterBinder binaryBinder = new TestBinaryParameterBinder();

	@Before
	public void before() {
	}

	@Test
	public void shouldRetainTransformerManager() {
		assertThat(TestSupport.<TransformerManager> getField(registry, "transformerManager"), is(transformerManager));
	}

	@Test
	public void shouldReturnNullWhenCannotBeBound() {
		Map<String, String[]> map = map("string", new String[] { "value" });
		assertThat(registry.createFor(new ParameterDescription("string", Void.class), new RequestDataMap(map)), is(nullValue()));
	}

	@Test
	public void shouldAllowAdditionOfBindersForBinderInstance() {
		Map<String, String[]> map = map("string", new String[] { "value" });
		assertThat(registry.createFor(new ParameterDescription("string", TestBindable.class), new RequestDataMap(map)), is(nullValue()));

		registry.addBinder(new TestParameterBinder());
		Object bindo = registry.createFor(new ParameterDescription("string", TestBindable.class), new RequestDataMap(map));
		assertThat(bindo instanceof TestBindable, is(true));
		assertThat(bindo, is((Object) new TestBindable("value")));
	}

	@Test
	public void shouldAllowRegistrationOfParameterBinders() {
		Map<String, String[]> map = map("bind", new String[] { "bound!" });

		assertThat(registry.createFor(new ParameterDescription("bind", TestBindable.class), new RequestDataMap(map)), is(nullValue()));

		registry.addBinder(binder);

		assertThat(registry.createFor(new ParameterDescription("bind", TestBindable.class), new RequestDataMap(map)), is((Object) new TestBindable("bound!")));
	}

	@Test
	public void shouldAllowRegistrationOfBinaryParameterBinders() {
		assertThat(registry.createFor(new ParameterDescription("bind", TestBindable.class), new MultipartFile("Bound", new byte[0], null)), is(nullValue()));

		registry.addBinder(binaryBinder);

		assertThat(registry.createFor(new ParameterDescription("bind", TestBindable.class), new MultipartFile("Bound", new byte[0], null)), is((Object) new TestBindable("Bound")));
	}

	@Test
	public void shouldAllowUnregistrationOfParameterBinders() {
		registry.addBinder(binder);
		registry.removeBinder(binder);
		Map<String, String[]> map = map("bind", new String[] { "bound!" });
		ParameterBinderRegistry registry = new ParameterBinderRegistry(transformerManager);
		assertThat(registry.createFor(new ParameterDescription("bind", TestBindable.class), new RequestDataMap(map)), is(nullValue()));
	}

	@Test
	public void shouldAllowUnregistrationOfBinaryParameterBinders() {
		registry.addBinder(binaryBinder);
		registry.removeBinder(binaryBinder);
		ParameterBinderRegistry registry = new ParameterBinderRegistry(transformerManager);
		assertThat(registry.createFor(new ParameterDescription("bind", TestBindable.class), new MultipartFile("Bound", new byte[0], null)), is(nullValue()));
	}

	@Test
	public void shouldNotFailOnMultipleUnregisterOfSameParameterBinder() {
		registry.addBinder(binder);
		registry.removeBinder(binder);
		registry.removeBinder(binder);
	}

	@Test
	public void shouldNotFailOnMultipleUnregisterOfSameBinaryParameterBinder() {
		registry.addBinder(binaryBinder);
		registry.removeBinder(binaryBinder);
		registry.removeBinder(binaryBinder);
	}

	@Test
	public void shouldNotFailOnUnregisterOfNonRegisteredParameterBinder() {
		registry.removeBinder(binder);
		registry.removeBinder((ParameterBinder<?>) null);
	}

	@Test
	public void shouldNotFailOnUnregisterOfNonRegisteredBinaryParameterBinder() {
		registry.removeBinder(binaryBinder);
		registry.removeBinder((BinaryParameterBinder<?>) null);
	}

	@Test
	public void shouldBindAllParametersNotAlreadyBound() {
		ParameterBinderRegistry.addDefaultBinders(registry);

		ParameterDescription parameterDescription1 = new ParameterDescription("param1", String.class);
		ParameterDescription parameterDescription2 = new ParameterDescription("param2", Integer.class);
		ParameterDescription parameterDescription3 = new ParameterDescription("param3", String.class);
		ParameterDescription parameterDescription4 = new ParameterDescription("param4", byte[].class);
		ParameterDescription parameterDescription5 = new ParameterDescription("param4", byte[].class);

		// @formatter:off
		Map<ParameterDescription, Object> bindings = mapKeys(parameterDescription1, parameterDescription2, parameterDescription3, parameterDescription4, parameterDescription5)
												.<Object> to(null, null,"Already bound", null, new byte[] { 3, 2, 1 });
		// @formatter:on

		Map<String, String[]> parameterMap = mapKeys("param1", "param2", "param3").to(array("value1"), array("2"), array("value3"));
		Map<String, MultipartFile> fileMap = mapKeys("param4", "param5").to(new MultipartFile("param4", new byte[] { 1, 2, 3 }, null), new MultipartFile("param5", new byte[] { 1, 2, 3 }, null));

		registry.bind(bindings, parameterMap, fileMap);

		assertThat(bindings.get(parameterDescription1), is((Object) "value1"));
		assertThat(bindings.get(parameterDescription2), is((Object) 2));
		assertThat(bindings.get(parameterDescription3), is((Object) "Already bound"));
		assertThat(bindings.get(parameterDescription4), is((Object) new byte[] { 1, 2, 3 }));
		assertThat(bindings.get(parameterDescription5), is((Object) new byte[] { 3, 2, 1 }));
	}

	private static class TestBindable {
		private String value;

		public TestBindable(String value) {
			this.value = value;
		}

		@Override
		public int hashCode() {
			return 0;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TestBindable other = (TestBindable) obj;
			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
			return true;
		}
	}

	private static class TestParameterBinder implements ParameterBinder<TestBindable> {
		@Override
		public boolean willBind(ParameterDescription parameterDescription, TransformerManager transformerManager) {
			return parameterDescription.isA(TestBindable.class);
		}

		@Override
		public TestBindable bind(ParameterBinderRegistry binders, ParameterDescription parameterDescription, RequestDataMap pathMap, TransformerManager transformerManager) {
			String[] strings = pathMap.get(parameterDescription.name());
			return new TestBindable(strings[0]);
		}
	}

	private static class TestBinaryParameterBinder implements BinaryParameterBinder<TestBindable> {

		@Override
		public boolean willBind(ParameterDescription parameterDescription) {
			return parameterDescription.isA(TestBindable.class);
		}

		@Override
		public TestBindable bind(ParameterDescription parameterDescription, MultipartFile file) {
			return new TestBindable(file.getName());
		}

	}
}
