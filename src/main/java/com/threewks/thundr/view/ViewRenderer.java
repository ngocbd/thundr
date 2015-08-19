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
package com.threewks.thundr.view;

import com.threewks.thundr.request.Request;
import com.threewks.thundr.request.Response;

/**
 * A {@link ViewRenderer} resolves the given view and writes it to the given response.
 * Typically this would be a servlet response (@see {@link ServletRequestPreservingViewRenderer}) or
 * into memory (@see {@link BasicViewRenderer}).
 * 
 * All {@link ViewRenderer} implementations should be stateless and safe for reuse across threads, requests or invocations.
 */
public interface ViewRenderer {
	// TODO - NAO - v3 - why is view rendering coupled to the request or response - is this an
	// internal concern? Request seems like its optional, it's mostly the model thats interesting except
	// in a servlet context. Response is really about capturing output - maybe that needs a higher level of
	// abstraction, like a renderedview?
	// If it is coupled, should this be a RequestContainer - and how are you expected to get it, or for that
	// matter the right implementation of ViewRenderer?
	public void render(Request request, Response response, Object view);
}
