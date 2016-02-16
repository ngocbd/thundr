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
package com.threewks.thundr.view.exception;

import com.threewks.thundr.http.exception.HttpStatusException;
import com.threewks.thundr.logger.Logger;
import com.threewks.thundr.request.Request;
import com.threewks.thundr.request.Response;
import com.threewks.thundr.view.ViewResolver;

public class HttpStatusExceptionViewResolver implements ViewResolver<HttpStatusException> {
	@Override
	public void resolve(Request req, Response resp, HttpStatusException viewResult) {
		Logger.info("Request %s resulted in an exception %s (%s) %s", req.getId(), viewResult.getClass().getSimpleName(), viewResult.getStatus(), viewResult.getMessage());
		sendError(resp, viewResult);
	}

	protected void sendError(Response resp, HttpStatusException viewResult) {
		// @formatter:off
		resp.withStatusCode(viewResult.getStatus())
			.withStatusMessage(viewResult.getMessage());
		// @formatter:on
	}
}
