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

import com.threewks.thundr.http.StatusCode;
import com.threewks.thundr.logger.Logger;
import com.threewks.thundr.request.Request;
import com.threewks.thundr.request.Response;
import com.threewks.thundr.view.ViewResolutionException;
import com.threewks.thundr.view.ViewResolver;

public class ExceptionViewResolver implements ViewResolver<Throwable> {

	@Override
	public void resolve(Request req, Response resp, Throwable viewResult) {
		try {
			Throwable exceptionOfInterest = getException(viewResult);
			logException(req, exceptionOfInterest);
			renderException(resp, exceptionOfInterest);
		} catch (Exception e) {
			throw new ViewResolutionException(e, "Failed to render an exception view because '%s' - original exception: %s", e.getMessage(), viewResult.getMessage());
		}
	}

	protected Throwable getException(Throwable viewResult) {
		Throwable exceptionOfInterest = viewResult;
		if (viewResult instanceof ViewResolutionException && viewResult.getCause() != null) {
			exceptionOfInterest = viewResult.getCause();
		}
		return exceptionOfInterest;
	}

	protected void renderException(Response resp, Throwable exceptionOfInterest) {
		// @formatter:off
		resp.withStatusCode(StatusCode.InternalServerError)
			.withStatusMessage(exceptionOfInterest.getMessage());
		// @formatter:on
	}

	protected void logException(Request req, Throwable exceptionOfInterest) {
		Logger.error(exceptionOfInterest, "Request %s resulted in an unhandled exception: %s", req.getId(), exceptionOfInterest.getMessage());
	}
}
