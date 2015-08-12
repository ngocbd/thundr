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
package com.threewks.thundr.aop;

import java.util.List;

public interface Arguments {
	public List<Object> getArguments();

	public <T> T getArgument(Class<T> type);

	public <T> T getArgument(String name);

	public <T> T getArgument(Class<T> type, String name);

	public <T> void replaceArgument(Class<T> type, T value);

	public <T> void replaceArgument(String name, T value);

	public <T> void replaceArgument(Class<T> type, String name, T value);

	public Object[] toArgs();
}
