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
package com.threewks.thundr.bind.parameter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import com.atomicleopard.expressive.EList;
import com.atomicleopard.expressive.EListImpl;
import com.threewks.thundr.collection.factory.SimpleCollectionFactory;
import com.threewks.thundr.collection.factory.SimpleMapFactory;
import com.threewks.thundr.http.MultipartFile;
import com.threewks.thundr.introspection.ParameterDescription;
import com.threewks.thundr.transformer.TransformerManager;

public class ParameterBinderRegistry {
	private static final String[] emptyStringArray = new String[0];

	private List<ParameterBinder<?>> binders = new ArrayList<ParameterBinder<?>>();
	private List<BinaryParameterBinder<?>> binaryBinders = new ArrayList<BinaryParameterBinder<?>>();

	private TransformerManager transformerManager;

	public ParameterBinderRegistry(TransformerManager transformerManager) {
		this.transformerManager = transformerManager;
	}

	/**
	 * Allows consumer code to introduce binding for specific types
	 * 
	 * @param binder
	 */
	public <T> ParameterBinderRegistry addBinder(ParameterBinder<T> binder) {
		binders.add(binder);
		return this;
	}

	/**
	 * Allows consumer code to introduce binding for specific types
	 * 
	 * @param binder
	 */
	public <T> ParameterBinderRegistry addBinder(BinaryParameterBinder<T> binder) {
		binaryBinders.add(binder);
		return this;
	}

	/**
	 * Removes the given binder which was previously registered. Requires the given object to be equal to the previously registered
	 * binder, so either the same instance or you need to implement equality
	 * 
	 * @param binder
	 * @return
	 */
	public <T> ParameterBinderRegistry removeBinder(ParameterBinder<T> binder) {
		this.binders.remove(binder);
		return this;
	}

	/**
	 * Removes the given binder which was previously registered. Requires the given object to be equal to the previously registered
	 * binder, so either the same instance or you need to implement equality
	 * 
	 * @param binder
	 * @return
	 */
	public <T> ParameterBinderRegistry removeBinder(BinaryParameterBinder<T> binder) {
		this.binaryBinders.remove(binder);
		return this;
	}

	public Object createFor(ParameterDescription parameterDescription, RequestDataMap pathMap) {
		for (ParameterBinder<?> binder : binders) {
			if (binder.willBind(parameterDescription, transformerManager)) {
				// return the first non-null object
				Object result = binder.bind(this, parameterDescription, pathMap, transformerManager);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}

	public Object createFor(ParameterDescription parameterDescription, MultipartFile file) {
		for (BinaryParameterBinder<?> binder : binaryBinders) {
			if (binder.willBind(parameterDescription)) {
				// return the first non-null object
				Object result = binder.bind(parameterDescription, file);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}

	public void bind(Map<ParameterDescription, Object> bindings, Map<String, String[]> parameterMap, Map<String, MultipartFile> fileMap) {
		RequestDataMap pathMap = new RequestDataMap(parameterMap);
		for (ParameterDescription parameterDescription : bindings.keySet()) {
			if (bindings.get(parameterDescription) == null) {
				String name = parameterDescription.name();
				MultipartFile multipartFile = fileMap == null ? null : fileMap.get(name);

				Object value = null;
				if (multipartFile != null) {
					value = createFor(parameterDescription, multipartFile);
				}
				if (value == null) {
					value = createFor(parameterDescription, pathMap);
				}
				if (value != null) {
					bindings.put(parameterDescription, value);
				}
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void addDefaultBinders(ParameterBinderRegistry registry) {
		registry.addBinder(new StringParameterBinder());
		registry.addBinder(new ArrayParameterBinder());
		registry.addBinder(new CollectionParameterBinder<ArrayList<Object>>(new SimpleCollectionFactory(ArrayList.class, ArrayList.class)));
		registry.addBinder(new CollectionParameterBinder<LinkedList<Object>>(new SimpleCollectionFactory(LinkedList.class, LinkedList.class)));
		registry.addBinder(new CollectionParameterBinder<EList<Object>>(new SimpleCollectionFactory(EList.class, EListImpl.class)));
		registry.addBinder(new CollectionParameterBinder<EListImpl<Object>>(new SimpleCollectionFactory(EListImpl.class, EListImpl.class)));
		registry.addBinder(new CollectionParameterBinder<TreeSet<Object>>(new SimpleCollectionFactory(TreeSet.class, TreeSet.class)));
		registry.addBinder(new CollectionParameterBinder<HashSet<Object>>(new SimpleCollectionFactory(HashSet.class, HashSet.class)));
		registry.addBinder(new CollectionParameterBinder<TreeSet<Object>>(new SimpleCollectionFactory(TreeSet.class, TreeSet.class)));
		registry.addBinder(new CollectionParameterBinder<SortedSet<Object>>(new SimpleCollectionFactory(SortedSet.class, TreeSet.class)));
		registry.addBinder(new CollectionParameterBinder<Set<Object>>(new SimpleCollectionFactory(Set.class, HashSet.class)));
		registry.addBinder(new CollectionParameterBinder<List<Object>>(new SimpleCollectionFactory(List.class, ArrayList.class)));
		registry.addBinder(new MapParameterBinder<Map<Object, Object>>(new SimpleMapFactory(HashMap.class, HashMap.class)));
		registry.addBinder(new MapParameterBinder<Map<Object, Object>>(new SimpleMapFactory(LinkedHashMap.class, HashMap.class)));
		registry.addBinder(new MapParameterBinder<Map<Object, Object>>(new SimpleMapFactory(TreeMap.class, TreeMap.class)));
		registry.addBinder(new MapParameterBinder<Map<Object, Object>>(new SimpleMapFactory(SortedMap.class, TreeMap.class)));
		registry.addBinder(new MapParameterBinder<Map<Object, Object>>(new SimpleMapFactory(Map.class, HashMap.class)));
		registry.addBinder(new CollectionParameterBinder<Collection<Object>>(new SimpleCollectionFactory(Collection.class, ArrayList.class)));
		registry.addBinder(new JavaBeanParameterBinder());
		registry.addBinder(new BasicTypesParameterBinder());
		registry.addBinder(new EnumParameterBinder());

		registry.addBinder(new ByteArrayBinaryParameterBinder());
		registry.addBinder(new MultipartFileParameterBinder());
		registry.addBinder(new InputStreamBinaryParameterBinder());
	}

	public static Map<String, String[]> convertListMapToArrayMap(Map<String, List<String>> formFields) {
		Map<String, String[]> parameterMap = new HashMap<String, String[]>();
		for (Map.Entry<String, List<String>> formFieldEntry : formFields.entrySet()) {
			parameterMap.put(formFieldEntry.getKey(), formFieldEntry.getValue().toArray(emptyStringArray));
		}
		return parameterMap;
	}
}
