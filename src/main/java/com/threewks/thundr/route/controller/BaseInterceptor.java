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
package com.threewks.thundr.route.controller;

import java.lang.annotation.Annotation;

import com.threewks.thundr.request.Request;
import com.threewks.thundr.request.Response;

public class BaseInterceptor<A extends Annotation> implements Interceptor<A> {

	@Override
	public <T> T before(A annotation, Request req, Response resp) {
		return null;
	}

	@Override
	public <T> T after(A annotation, Object view, Request req, Response resp) {
		return null;
	}

	@Override
	public <T> T exception(A annotation, Exception e, Request req, Response resp) {
		return null;
	}
}
