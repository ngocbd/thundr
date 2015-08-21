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
package com.threewks.thundr.bind;

import java.util.LinkedHashMap;
import java.util.Map;

public class BinderRegistry {
	private Map<Class<? extends Binder>, Binder> methodBinders = new LinkedHashMap<Class<? extends Binder>, Binder>();

	public BinderRegistry() {
	}

	public void add(Binder binder) {
		methodBinders.put(binder.getClass(), binder);
	}

	public boolean contains(Class<? extends Binder> type) {
		return methodBinders.containsKey(type);
	}

	public void remove(Class<? extends Binder> type) {
		methodBinders.remove(type);
	}

	public Iterable<Binder> list() {
		return methodBinders.values();
	}
}
