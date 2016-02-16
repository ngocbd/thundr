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
package com.threewks.thundr.view;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.threewks.thundr.http.StatusCode;

/**
 * {@link ExceptionView} wraps an exception so that unhandled exceptions have a consistent underlying model.
 */
public class ExceptionView {
	private String message;
	private StatusCode statusCode;
	private String stackTrace;

	public ExceptionView(Throwable e) {
		this.stackTrace = ExceptionUtils.getStackTrace(e);
		this.message = e.getMessage();
		this.statusCode = StatusCode.InternalServerError;
	}

	public ExceptionView(StatusCode statusCode, String message, String stackTrace) {
		super();
		this.message = message;
		this.statusCode = statusCode;
		this.stackTrace = stackTrace;
	}

	public String getMessage() {
		return message;
	}

	public StatusCode getStatusCode() {
		return statusCode;
	}

	public String getStackTrace() {
		return stackTrace;
	}

}
