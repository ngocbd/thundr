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

import java.util.UUID;

/**
 * A {@link ThreadLocalRequestContainer} stores the {@link Request} and {@link Response} in a threadlocal.
 * 
 * This is suitable for containers (such as standard servlet containers) that run a request per thread model.
 */
public class ThreadLocalRequestContainer implements RequestContainer, MutableRequestContainer {
	private ThreadLocal<Request> req = new ThreadLocal<Request>();
	private ThreadLocal<Response> resp = new ThreadLocal<Response>();

	@Override
	public UUID getId() {
		Request request = getRequest();
		return request == null ? null : request.getId();
	}

	@Override
	public Request getRequest() {
		return req.get();
	}

	@Override
	public Response getResponse() {
		return resp.get();
	}

	@Override
	public void set(Request req, Response resp) {
		this.req.set(req);
		this.resp.set(resp);
	}

	@Override
	public void clear() {
		this.set(null, null);
	}
}
