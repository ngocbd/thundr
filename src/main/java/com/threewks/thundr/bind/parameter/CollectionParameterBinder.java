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

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.threewks.thundr.collection.factory.CollectionFactory;
import com.threewks.thundr.introspection.ParameterDescription;
import com.threewks.thundr.introspection.TypeIntrospector;
import com.threewks.thundr.transformer.TransformerManager;

/**
 * Binds to a collection using the given Factory. Supports two types of list - indexed and unindexed.
 * An indexed list looks like this:
 * list[0]=value
 * list[1]=value
 * 
 * and unindexed list looks like this:
 * list=value,value
 * 
 */
public class CollectionParameterBinder<T extends Collection<Object>> implements ParameterBinder<T> {
	private static final Pattern indexPattern = Pattern.compile("\\[(\\d+)\\]");
	private CollectionFactory<T> collectionFactory;

	public CollectionParameterBinder(CollectionFactory<T> collectionFactory) {
		this.collectionFactory = collectionFactory;
	}

	@Override
	public T bind(ParameterBinderRegistry binders, ParameterDescription parameterDescription, RequestDataMap pathMap, TransformerManager transformerManager) {
		List<String> entryForParameter = pathMap.get(parameterDescription.name());
		entryForParameter = entryForParameter == null ? pathMap.get(parameterDescription.name() + "[]") : entryForParameter;
		boolean isIndexed = entryForParameter == null || entryForParameter.size() == 0;
		return isIndexed ? createIndexed(binders, parameterDescription, pathMap, transformerManager) : createUnindexed(binders, parameterDescription, pathMap, transformerManager);
	}

	// TODO - Is there a discrepency between how unindexed and indexed entities are created?
	// createUnindexed uses the TransformerManager directly, but createIndexed uses the ParameterBinderRegistry?
	private T createUnindexed(ParameterBinderRegistry binders, ParameterDescription parameterDescription, RequestDataMap pathMap, TransformerManager transformerManager) {
		List<String> entries = pathMap.get(parameterDescription.name());
		entries = entries == null ? pathMap.get(parameterDescription.name() + "[]") : entries;
		// a special case of a single empty string entry we'll equate to null
		if (entries == null || entries.size() == 1 && (entries.get(0) == null || "".equals(entries.get(0)))) {
			return null;
		}
		T listParameter = collectionFactory.create();
		Type type = parameterDescription.getGenericType(0);
		Class<?> clazz = TypeIntrospector.asClass(type);
		for (String entry : entries) {
			Object listEntry = transformerManager.transform(String.class, clazz, entry);
			listParameter.add(listEntry);
		}
		return listParameter;
	}

	private T createIndexed(ParameterBinderRegistry binders, ParameterDescription parameterDescription, RequestDataMap pathMap, TransformerManager transformerManager) {
		pathMap = pathMap.pathMapFor(parameterDescription.name());
		Set<String> uniqueChildren = pathMap.uniqueChildren();
		if (uniqueChildren.size() == 0) {
			return null;
		}

		Map<Integer, String> keyToIndex = new HashMap<Integer, String>();
		int highestIndex = 0;
		for (String string : uniqueChildren) {
			Matcher matcher = indexPattern.matcher(string);
			if (!matcher.matches()) {
				throw new IllegalArgumentException(String.format("Cannot bind %s %s - not a valid list index", parameterDescription.name(), string));
			}
			String indexString = matcher.group(1);
			int index = Integer.parseInt(indexString);
			keyToIndex.put(index, string);
			highestIndex = Math.max(highestIndex, index);
		}
		highestIndex += 1;

		T listParameter = collectionFactory.create();
		for (int i = 0; i < highestIndex; i++) {
			String key = keyToIndex.get(i);
			if (key != null) {
				ParameterDescription parameter = new ParameterDescription(key, parameterDescription.getGenericType(0));
				Object listEntry = binders.createFor(parameter, pathMap);
				listParameter.add(listEntry);
			} else {
				listParameter.add(null);
			}
		}
		return listParameter;
	}

	@Override
	public boolean willBind(ParameterDescription parameterDescription, TransformerManager transformerManager) {
		return parameterDescription.isA(collectionFactory.forType());
	}

	@Override
	public String toString() {
		return this.getClass() + " for " + collectionFactory.forType();
	}
}
