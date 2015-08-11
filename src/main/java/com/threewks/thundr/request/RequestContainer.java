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
 * A {@link RequestContainer} retains the {@link Request} and {@link Response} for the duration of the request.
 * It also provides a unique id which can be used for logging and correlation
 * 
 * This is useful for classes that require the {@link Request} or {@link Response} but
 * whose call stack is a long way from a controller method. In general, applications should prefer
 * to pass the request or response down as method arguments, however in circumstances when this
 * causes excessive coupling, it may be preferable to use the {@link RequestContainer} class.
 */
public interface RequestContainer {
	
	/**
	 * @return the unique id for this request
	 */
	public UUID getId();
	
	/**
	 * Get the {@link Request} object for the current request
	 * 
	 * @return the current request, or null if there is none
	 */
	public Request getRequest();

	/**
	 * Get the {@link Response} object for the current request
	 * 
	 * @return the current response, or null if there is none
	 */
	public Response getResponse();
}
