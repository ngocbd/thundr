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
package com.threewks.thundr.aop;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import com.atomicleopard.expressive.Cast;
import com.threewks.thundr.introspection.MethodIntrospector;
import com.threewks.thundr.introspection.TypeIntrospector;

public class ArgumentsImpl implements Arguments {
	private MethodIntrospector methodIntrospector;
	private List<Object> arguments;

	public ArgumentsImpl(MethodIntrospector methodIntrospector, Object[] args) {
		this.arguments = Arrays.asList(args);
		this.methodIntrospector = methodIntrospector;
	}

	@Override
	public List<Object> getArguments() {
		return arguments;
	}

	@Override
	public <T> T getArgument(Class<T> type) {
		int index = find(type);
		return this.<T> atIndex(index);
	}

	@Override
	public <T> T getArgument(String name) {
		int index = find(name);
		return this.<T> atIndex(index);
	}

	@Override
	public <T> T getArgument(Class<T> type, String name) {
		int index = find(type, name);
		return this.<T> atIndex(index);
	}

	@Override
	public <T> void replaceArgument(Class<T> type, T value) {
		int index = find(type);
		replace(value, index);
	}

	@Override
	public <T> void replaceArgument(String name, T value) {
		int index = find(name);
		replace(value, index);
	}

	@Override
	public <T> void replaceArgument(Class<T> type, String name, T value) {
		int index = find(type, name);
		replace(value, index);
	}

	@Override
	public Object[] toArgs() {
		return this.arguments.toArray();
	}

	@SuppressWarnings("unchecked")
	protected <T> T atIndex(int index) {
		return index < 0 ? null : (T) arguments.get(index);
	}

	protected <T> void replace(T value, int index) {
		// TODO - NAO - In theory we could use transformer magic to make this happen, and probably just make boxed types just work.
		// Best to wait for feedback
		if (index >= 0) {
			Class<?> type = this.methodIntrospector.getClassTypes().get(index);
			Class<? extends Object> givenType = value == null ? null : value.getClass();
			if (value == null || type.isAssignableFrom(givenType) || TypeIntrospector.canBoxOrUnbox(type, givenType)) {
				this.arguments.set(index, value);
			} else {
				String message = "Cannot replace parameter '%s %s' with a %s '%s' - the types are not compatible";
				throw new ClassCastException(String.format(message, type.getName(), this.methodIntrospector.getName(index), value == null ? "" : givenType.getName(), value));
			}
		}
	}

	protected <T> int find(Class<T> type) {
		int index = methodIntrospector.getTypes().indexOf(type);
		if (index < 0 && TypeIntrospector.isABasicType(type)) {
			index = methodIntrospector.getTypes().indexOf(TypeIntrospector.box(type));
		}
		if (index < 0 && TypeIntrospector.isABoxedType(type)) {
			index = methodIntrospector.getTypes().indexOf(TypeIntrospector.unbox(type));
		}
		return index;
	}

	protected int find(String name) {
		return methodIntrospector.getNames().indexOf(name);
	}

	protected <T> int find(Class<T> type, String name) {
		int index = find(name);
		if (index >= 0) {
			Type actualType = methodIntrospector.getTypes().get(index);
			Class<?> actualClass = Cast.as(actualType, Class.class);
			index = actualClass != null && type.isAssignableFrom(actualClass) ? index : -1;
		}
		return index;
	}

}
