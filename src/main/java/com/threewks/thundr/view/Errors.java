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
package com.threewks.thundr.view;

import java.util.LinkedHashMap;
import java.util.Map;

import com.atomicleopard.expressive.Expressive;

/**
 * {@link Errors} is a generic model for representing errors, which is just a convenience wrapper for a {@link Map}.
 * 
 * The core distinction between {@link Errors} and {@link Model} is that this class represents a mapping of &lt;String, String&gt;
 * and has a semantic use.
 */
public class Errors extends LinkedHashMap<String, String> {
	private static final long serialVersionUID = 1L;

	public Errors() {
		super();
	}

	/**
	 * Creates {@link Errors} from the given entries
	 * 
	 * @param m
	 */
	public Errors(Map<? extends String, ? extends String> m) {
		super(m);
	}

	/**
	 * Creates {@link Errors} from the given entries, where the arguments are taken to be in
	 * the form 'key1', 'value1', 'key2', 'value2' ...
	 * 
	 * @param args
	 */
	public Errors(String... args) {
		super(Expressive.<String, String> map((Object[]) args));
	}

	public boolean isNotEmpty() {
		return !isEmpty();
	}

	/**
	 * Same as {@link Errors#put(String, String)} but returns this {@link Errors} object for convenience.
	 * 
	 * @param key
	 * @param value
	 * @return this errors object after being updated with the given key/value pair
	 */
	public Errors with(String key, String value) {
		this.put(key, value);
		return this;
	}

	/**
	 * Static factory method for creating a new empty {@link Errors} object.
	 * 
	 * @return
	 */
	public static Errors None() {
		return new Errors();
	}
}
