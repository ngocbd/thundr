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
package com.threewks.thundr.request;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.atomicleopard.expressive.Cast;
import com.atomicleopard.expressive.ETransformer;
import com.threewks.thundr.http.ContentType;
import com.threewks.thundr.http.Cookie;
import com.threewks.thundr.http.StatusCode;
import com.threewks.thundr.transformer.TransformerManager;

public abstract class BaseResponse implements Response {
	protected TransformerManager transformerManager;

	public BaseResponse(TransformerManager transformerManager) {
		super();
		this.transformerManager = transformerManager;
	}

	protected abstract Object getRawResponse();

	protected abstract void setHeaderInternal(String key, List<String> values);

	@Override
	public boolean isUncommitted() {
		return !isCommitted();
	}
	
	@Override
	public <T> T getRawResponse(Class<T> type) {
		return Cast.as(getRawResponse(), type);
	}

	@Override
	public Response withHeader(String header, Object value) {
		ETransformer<Object, String> transformer = (ETransformer<Object, String>) transformerManager.getBestTransformer(value.getClass(), String.class);
		String stringValue = transformer.from(value);
		setHeaderInternal(header, Collections.singletonList(stringValue));
		return this;
	}

	@Override
	public Response withHeader(String header, Object value, boolean include) {
		return include ? withHeader(header, value) : this;
	}

	@Override
	public Response withHeader(String header, Object... values) {
		return withHeader(header, Arrays.asList(values));
	}

	@Override
	public Response withHeader(String header, Collection<Object> values, boolean include) {
		return include ? withHeader(header, values) : this;
	}

	@Override
	public Response withHeaders(Map<String, Object> headers) {
		headers.forEach(this::withHeader);
		return this;
	}

	// TODO - v3 - make a specific type that wraps dates for RFC spec date formats
	// so that users can send X-Last-Updated sensibly, but specific http dates in the right format
	@Override
	public Response withHeader(String header, ZonedDateTime value) {
		throw new UnsupportedOperationException("To be implemented");
		// resp.addDateHeader(header, value.toInstant().toEpochMilli());
		// return this;
	}

	@Override
	public Response withHeader(String header, Collection<Object> values) {
		List<String> stringValues = values.stream().map(value -> {
			ETransformer<Object, String> transformer = (ETransformer<Object, String>) transformerManager.getBestTransformer(value.getClass(), String.class);
			return transformer.from(value);
		}).collect(Collectors.toList());
		setHeaderInternal(header, stringValues);
		return this;
	}

	@Override
	public Response withStatusCode(StatusCode statusCode, boolean include) {
		return include ? withStatusCode(statusCode) : this;
	}

	@Override
	public Response withContentType(ContentType contentType) {
		return withContentType(contentType.value());
	}

	@Override
	public Response withContentType(String contentType, boolean include) {
		return include ? withContentType(contentType) : this;
	}

	@Override
	public Response withCookies(Cookie... cookies) {
		return withCookies(Arrays.asList(cookies));
	}

	@Override
	public Response withCharacterEncoding(String characterEncoding, boolean include) {
		return include ? withCharacterEncoding(characterEncoding) : this;
	}
}
