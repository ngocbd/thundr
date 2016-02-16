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
package com.threewks.thundr.request;

import java.util.UUID;

import com.atomicleopard.expressive.Cast;
import com.threewks.thundr.http.ContentType;
import com.threewks.thundr.route.HttpMethod;
import com.threewks.thundr.route.Route;

public abstract class BaseRequest implements Request {
	protected HttpMethod httpMethod;
	protected UUID id;
	protected Route route;

	public BaseRequest(HttpMethod httpMethod, Route route) {
		this.id = UUID.randomUUID();
		this.httpMethod = httpMethod;
		this.route = route;
	}

	protected abstract Object getRawRequest();

	@Override
	public UUID getId() {		
		return id;
	}

	@Override
	public <T> T getRawRequest(Class<T> type) {
		return Cast.as(getRawRequest(), type);
	}

	@Override
	public ContentType getContentType() {
		return ContentType.from(getContentTypeString());
	}

	@Override
	public HttpMethod getMethod() {
		return httpMethod;
	}

	@Override
	public boolean isA(HttpMethod method) {
		return httpMethod == method;
	}

	@Override
	public Route getRoute() {
		return route;
	}
}
