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
package com.threewks.thundr.bind.json;

import java.io.BufferedReader;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.atomicleopard.expressive.Expressive;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.threewks.thundr.bind.BindException;
import com.threewks.thundr.bind.Binder;
import com.threewks.thundr.bind.http.request.CookieBinder;
import com.threewks.thundr.bind.http.request.RequestClassBinder;
import com.threewks.thundr.http.ContentType;
import com.threewks.thundr.introspection.ParameterDescription;
import com.threewks.thundr.introspection.TypeIntrospector;
import com.threewks.thundr.json.GsonSupport;
import com.threewks.thundr.request.Request;
import com.threewks.thundr.request.Response;

public class GsonBinder implements Binder {
	/**
	 * When trying to bind to a Pojo/DTO, we know there is a specific set of objects that we shouldn't bother trying with. This list contains those types.
	 */
	public static final List<Class<?>> NonBindableTypes = Expressive.list(RequestClassBinder.BoundTypes).addItems(CookieBinder.BoundTypes).addItems(Boolean.class, boolean.class);
	/**
	 * When trying to bind to the individual parameters of the json body, there are parameters that imply the controller wants to handle the request body itself and mean this binder shouldnt consume
	 * the input stream.
	 */
	// TODO - v3 - can this be shared somewhere, so that other modules can register against them (for HttpServletRequest)
	public static final List<Class<?>> TypesIndicatingBindingShouldBeSkipped = Expressive.<Class<?>> list(Request.class);

	private GsonBuilder gsonBuilder;

	public GsonBinder() {
		this(GsonSupport.createBasicGsonBuilder());
	}

	public GsonBinder(GsonBuilder gsonBuilder) {
		this.gsonBuilder = gsonBuilder;
	}

	/**
	 * Exposes the underlying builder, allowing the modification of how Json is bound.
	 * 
	 * @return
	 */
	public GsonBuilder getGsonBuilder() {
		return gsonBuilder;
	}

	public boolean canBind(ContentType contentType) {
		return ContentType.ApplicationJson == contentType;
	}

	@Override
	public void bindAll(Map<ParameterDescription, Object> bindings, Request req, Response resp, Map<String, String> pathVariables) {
		if (!bindings.isEmpty() && bindings.containsValue(null)) {
			if (canBind(req.getContentType())) {
				ParameterDescription jsonParameterDescription = findParameterDescriptionForJsonParameter(bindings);
				Gson gson = gsonBuilder.create();
				if (jsonParameterDescription != null) {
					bindToSingleParameter(bindings, req, gson, jsonParameterDescription);
				} else {
					bindToUnboundParameters(bindings, req, gson);
				}
			}
		}
	}

	private void bindToUnboundParameters(Map<ParameterDescription, Object> bindings, Request req, Gson gson) {
		if (shouldBindToUnboundParameters(bindings)) {
			try {
				BufferedReader reader = req.getReader();
				if (reader != null) {
					JsonObject json = new JsonParser().parse(reader).getAsJsonObject();

					for (Map.Entry<ParameterDescription, Object> entry : bindings.entrySet()) {
						if (entry.getValue() == null) {
							ParameterDescription parameterDescription = entry.getKey();
							try {
								JsonElement jsonElement = json.get(parameterDescription.name());
								if (jsonElement != null) {
									Object value = gson.fromJson(jsonElement, parameterDescription.type());
									bindings.put(parameterDescription, value);
								}
							} catch (Exception e) {
								throw new BindException(e, "Failed to bind parameter '%s' as %s using JSON: %s", parameterDescription.name(), parameterDescription.type(), e.getMessage());
							}
						}
					}
				}
			} catch (BindException e) {
				throw e;
			} catch (Exception e) {
				throw new BindException(e, "Failed to bind JSON: %s", e.getMessage());
			}
		}
	}

	protected boolean shouldBindToUnboundParameters(Map<ParameterDescription, Object> bindings) {
		for (ParameterDescription parameterDescription : bindings.keySet()) {
			if (TypesIndicatingBindingShouldBeSkipped.contains(parameterDescription.classType())) {
				return false;
			}
		}
		return true;
	}

	private void bindToSingleParameter(Map<ParameterDescription, Object> bindings, Request req, Gson gson, ParameterDescription jsonParameterDescription) {
		try {
			BufferedReader reader = req.getReader();
			if (reader != null) {
				Object converted = gson.fromJson(reader, jsonParameterDescription.type());
				bindings.put(jsonParameterDescription, converted);
			}
		} catch (Exception e) {
			throw new BindException(e, "Failed to bind parameter '%s' as %s using JSON: %s", jsonParameterDescription.name(), jsonParameterDescription.type(), e.getMessage());
		}
	}

	private ParameterDescription findParameterDescriptionForJsonParameter(Map<ParameterDescription, Object> bindings) {
		for (Map.Entry<ParameterDescription, Object> bindingEntry : bindings.entrySet()) {
			ParameterDescription parameterDescription = bindingEntry.getKey();
			Class<?> type = parameterDescription.classType();
			boolean isUnbound = bindingEntry.getValue() == null;
			boolean isABindableType = !NonBindableTypes.contains(parameterDescription.type());
			boolean isACollection = Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type);
			boolean isAJavabean = TypeIntrospector.isAJavabean(type);
			if (isUnbound && isABindableType && (isACollection || isAJavabean)) {
				return parameterDescription;
			}
		}
		return null;
	}
}
