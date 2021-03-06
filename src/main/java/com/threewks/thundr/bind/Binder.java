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
package com.threewks.thundr.bind;

import java.util.List;
import java.util.Map;

import com.atomicleopard.expressive.Expressive;
import com.threewks.thundr.bind.parameter.ParameterBinder;
import com.threewks.thundr.introspection.ParameterDescription;
import com.threewks.thundr.request.Request;
import com.threewks.thundr.request.Response;

/**
 * Used to control how one or more parameters have data bound to them on controller methods.
 * 
 * 
 * An {@link Binder} can be considered a source of data, for example a request, cookies, the session,
 * or the binary content of the request stream.
 * 
 * This is distinct from the {@link ParameterBinder}, which allows conversion of data to a specific argument.
 */
public interface Binder {
	/**
	 * The entries in this list represent types which indicate that the receiving controller may want to consume, or 'own'
	 * the request input stream. Their presence can control whether specific {@link Binder} implementations wish
	 * to skip consuming the request body to prevent causing errors.
	 */
	public static final List<Class<?>> RequestBodyConsumingTypes = Expressive.<Class<?>> list(Request.class);

	/**
	 * When invoked, implementors can bind any of the given bindings whose value is null (that is those parameters that are not already bound).
	 * Data available to be bound can be retrieved from the request, the response or the pathVariables parameter.
	 * 
	 * @param bindings
	 * @param req
	 * @param resp
	 */
	public void bindAll(Map<ParameterDescription, Object> bindings, Request req, Response resp);
}
