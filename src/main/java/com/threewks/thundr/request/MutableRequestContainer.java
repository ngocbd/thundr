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

/**
 * A {@link MutableRequestContainer} defines the ability to modify the request and response provided by a {@link RequestContainer}.
 * This will not be needed in application code, and using it in this capacity could have catastrophic consequences.
 */
public interface MutableRequestContainer extends RequestContainer {
	/**
	 * Sets both the request and response for the current request. Application code should never need to call this
	 * directly. Be aware that doing so could have catastrophic consequences.
	 * 
	 * @param req
	 * @param resp
	 */
	public void set(Request req, Response resp);

	/**
	 * Clears both the request and response for the current request. Application code should never need to call this
	 * directly. Be aware that doing so could have catastrophic consequences.
	 */
	public void clear();
}
