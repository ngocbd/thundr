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
package com.threewks.thundr.bind.parameter;

import static com.atomicleopard.expressive.Expressive.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.threewks.thundr.bind.BindException;
import com.threewks.thundr.introspection.ParameterDescription;
import com.threewks.thundr.transformer.TransformerManager;

public class JavaBeanParameterBinderTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private JavaBeanParameterBinder binder = new JavaBeanParameterBinder();
	private TransformerManager transformerManager = TransformerManager.createWithDefaults();
	private ParameterBinderRegistry binders = new ParameterBinderRegistry(transformerManager);

	@SuppressWarnings("unchecked")
	@Test
	public void shouldBindABasicJavabean() {
		ParameterDescription parameterDescription = new ParameterDescription("bean", BasicTestBean.class);
		Map<String, List<String>> map = mapKeys("bean.name", "bean.id").to(values("string"), values("1"));

		Object result = binder.bind(binders, parameterDescription, new RequestDataMap(map), transformerManager);
		assertThat(result, is(notNullValue()));
		assertThat(result, instanceOf(BasicTestBean.class));
		BasicTestBean bean = (BasicTestBean) result;
		assertThat(bean.getName(), is("string"));
		assertThat(bean.getId(), is(1l));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldBindABasicJavabeanWithExtendedTypes() {
		ParameterDescription parameterDescription = new ParameterDescription("bean", BasicTestBean.class);
		Map<String, List<String>> map = mapKeys("bean.name", "bean.id", "bean.dateTime").to(values("string"), values("1"), values("2014-06-02T12:01:01.001Z"));

		Object result = binder.bind(binders, parameterDescription, new RequestDataMap(map), transformerManager);
		assertThat(result, is(notNullValue()));
		assertThat(result, instanceOf(BasicTestBean.class));
		BasicTestBean bean = (BasicTestBean) result;
		assertThat(bean.getName(), is("string"));
		assertThat(bean.getId(), is(1l));
		assertThat(bean.getDateTime(), is(new DateTime(2014, 6, 2, 12, 1, 1, 1).withZoneRetainFields(DateTimeZone.UTC)));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldBindANestedJavabean() {
		ParameterDescription parameterDescription = new ParameterDescription("bean", NestedTestBean.class);
		Map<String, List<String>> map = mapKeys("bean.name", "bean.nested.name", "bean.nested.id").to(values("outer"), values("inner"), values("1"));

		Object result = binder.bind(binders, parameterDescription, new RequestDataMap(map), transformerManager);
		assertThat(result, is(notNullValue()));
		assertThat(result, instanceOf(NestedTestBean.class));
		NestedTestBean bean = (NestedTestBean) result;
		assertThat(bean.getName(), is("outer"));
		assertThat(bean.getNested(), is(notNullValue()));
		assertThat(bean.getNested(), instanceOf(BasicTestBean.class));
		assertThat(bean.getNested().getName(), is("inner"));
		assertThat(bean.getNested().getId(), is(1l));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldBindAMultidimensonalJavabean() {
		ParameterDescription parameterDescription = new ParameterDescription("bean", MultidimensionalTestBean.class);
		Map<String, List<String>> map = mapKeys("bean.name", "bean.nested[0].name", "bean.nested[0].id", "bean.nested[1].name", "bean.nested[1].id").to(values("outer"), values("inner1"), values("1"),
				values("inner2"), values("2"));

		Object result = binder.bind(binders, parameterDescription, new RequestDataMap(map), transformerManager);
		assertThat(result, is(notNullValue()));
		assertThat(result, instanceOf(MultidimensionalTestBean.class));
		MultidimensionalTestBean bean = (MultidimensionalTestBean) result;
		assertThat(bean.getName(), is("outer"));
		List<BasicTestBean> nested = bean.getNested();
		assertThat(nested, is(notNullValue()));
		assertThat(nested.size(), is(2));
		assertThat(nested.get(0).getName(), is("inner1"));
		assertThat(nested.get(0).getId(), is(1l));
		assertThat(nested.get(1).getName(), is("inner2"));
		assertThat(nested.get(1).getId(), is(2l));
	}

	@Test
	public void shouldThrowBindExceptionWhenCannotBind() {
		thrown.expect(BindException.class);
		thrown.expectMessage("Failed to bind onto class com.threewks.thundr.bind.parameter.JavaBeanParameterBinderTest$UnbindableTestBean: Intentional");

		ParameterDescription parameterDescription = new ParameterDescription("bean", UnbindableTestBean.class);
		Map<String, List<String>> map = map("bean.value", values("value"));

		binder.bind(binders, parameterDescription, new RequestDataMap(map), transformerManager);
	}

	@Test
	public void shouldReturnNullIfNoDataInRequestMap() {
		ParameterDescription parameterDescription = new ParameterDescription("bean", UnbindableTestBean.class);
		Map<String, List<String>> map = map("other.data", values("value"));

		Object result = binder.bind(binders, parameterDescription, new RequestDataMap(map), transformerManager);
		assertThat(result, is(nullValue()));
	}

	@Test
	public void shouldOnlyBindToJavabeans() {
		assertThat(binder.willBind(new ParameterDescription(null, DateTime.class), transformerManager), is(true));
		assertThat(binder.willBind(new ParameterDescription(null, BasicTestBean.class), transformerManager), is(true));
		assertThat(binder.willBind(new ParameterDescription(null, NestedTestBean.class), transformerManager), is(true));

		assertThat(binder.willBind(new ParameterDescription(null, String.class), transformerManager), is(false));
		assertThat(binder.willBind(new ParameterDescription(null, Long.class), transformerManager), is(false));
		assertThat(binder.willBind(new ParameterDescription(null, byte.class), transformerManager), is(false));
		assertThat(binder.willBind(new ParameterDescription(null, List.class), transformerManager), is(false));
		assertThat(binder.willBind(new ParameterDescription(null, Collection.class), transformerManager), is(false));
		assertThat(binder.willBind(new ParameterDescription(null, Map.class), transformerManager), is(false));
		assertThat(binder.willBind(new ParameterDescription(null, Set.class), transformerManager), is(false));
	}

	public static List<String> values(String... args) {
		return Arrays.asList(args);
	}

	public static class MultidimensionalTestBean {
		private String name;
		private List<BasicTestBean> nested;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public List<BasicTestBean> getNested() {
			return nested;
		}

		public void setNested(List<BasicTestBean> nested) {
			this.nested = nested;
		}
	}

	public static class NestedTestBean {
		private String name;
		private BasicTestBean nested;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public BasicTestBean getNested() {
			return nested;
		}

		public void setNested(BasicTestBean nested) {
			this.nested = nested;
		}
	}

	public static class BasicTestBean {
		private String name;
		private Long id;
		private DateTime dateTime;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public DateTime getDateTime() {
			return dateTime;
		}

		public void setDateTime(DateTime dateTime) {
			this.dateTime = dateTime;
		}
	}

	static class UnbindableTestBean {
		public UnbindableTestBean() {
			throw new RuntimeException("Intentional");
		}
	}
}
