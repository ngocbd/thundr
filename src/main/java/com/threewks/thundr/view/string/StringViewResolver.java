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
package com.threewks.thundr.view.string;

import java.io.OutputStream;

import com.threewks.thundr.request.Request;
import com.threewks.thundr.request.Response;
import com.threewks.thundr.view.BaseView;
import com.threewks.thundr.view.ViewResolutionException;
import com.threewks.thundr.view.ViewResolver;

public class StringViewResolver implements ViewResolver<StringView> {

	public StringViewResolver() {
	}

	@Override
	public void resolve(Request req, Response resp, StringView viewResult) {
		try {
			byte[] data = viewResult.contentBytes();
			BaseView.applyToResponse(viewResult, resp);
			resp.withContentLength(data.length)
				.finaliseHeaders();
			OutputStream outputStream = resp.getOutputStream();
			outputStream.write(data);
			outputStream.flush();
		} catch (Exception e) {
			throw new ViewResolutionException(e, "Failed to write String view result: %s", e.getMessage());
		}
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
}
