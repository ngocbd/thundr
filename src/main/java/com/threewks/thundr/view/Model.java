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
package com.threewks.thundr.view;

import java.util.LinkedHashMap;
import java.util.Map;

import com.atomicleopard.expressive.Expressive;

/**
 * A generic model which is just a {@link Map}, with convenience constructors and methods
 */
public class Model extends LinkedHashMap<String, Object> {
	private static final long serialVersionUID = 1L;

	public Model() {
		super();
	}

	public Model(Map<? extends String, ? extends Object> m) {
		super(m);
	}

	public Model(Object... args) {
		super(Expressive.<String, Object> map(args));
	}

	/**
	 * Returns a new model which overlays the given models on top of eachther, with later models taking precendence.
	 */
	@SafeVarargs
	public static <M extends Map<String, Object>> Model combine(M... models) {
		Model result = new Model();
		for (M m : models) {
			result.putAll(m);
		}
		return result;
	}
}
