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
package com.threewks.thundr.view.json;

import java.io.OutputStream;

import com.atomicleopard.expressive.Cast;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.threewks.thundr.json.GsonSupport;
import com.threewks.thundr.request.Request;
import com.threewks.thundr.request.Response;
import com.threewks.thundr.view.BaseView;
import com.threewks.thundr.view.ViewResolutionException;
import com.threewks.thundr.view.ViewResolver;

public class JsonViewResolver implements ViewResolver<JsonView> {
	private GsonBuilder gsonBuilder;

	public JsonViewResolver() {
		this(GsonSupport.createBasicGsonBuilder());
	}

	public JsonViewResolver(GsonBuilder gsonBuilder) {
		this.gsonBuilder = gsonBuilder;
	}

	/**
	 * Exposes the underlying gson builder, allowing modification of the properties controlling how json is serialized.
	 * 
	 * @return
	 */
	public GsonBuilder getGsonBuilder() {
		return gsonBuilder;
	}

	@Override
	public void resolve(Request req, Response resp, JsonView viewResult) {
		Object output = viewResult.getOutput();
		try {
			Gson create = gsonBuilder.create();
			JsonElement jsonElement = Cast.as(output, JsonElement.class);
			String json = jsonElement == null ? create.toJson(output) : create.toJson(jsonElement);
			String encoding = viewResult.getCharacterEncoding();
			byte[] data = json.getBytes(encoding);
			BaseView.applyToResponse(viewResult, resp);
			resp.withContentLength(data.length);
			OutputStream outputStream = resp.getOutputStream();
			outputStream.write(data);
			outputStream.flush();
			// @formatter:on
		} catch (Exception e) {
			throw new ViewResolutionException(e, "Failed to generate JSON output for object '%s': %s", output.toString(), e.getMessage());
		}
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
}
