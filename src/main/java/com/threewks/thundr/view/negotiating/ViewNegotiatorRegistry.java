/*
 * This file is a component of thundr, a software library from 3wks.
 * Read more: http://www.3wks.com.au/thundr
 * Copyright (C) 2013 3wks, <thundr@3wks.com.au>
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
package com.threewks.thundr.view.negotiating;

import java.util.List;

/**
 * This registry holds mapping between content types and {@link Negotiator} instances. These are used to
 * determine the what view to respond to when using a {@link NegotiatingView}/{@link NegotiatingViewResolver}.
 */
public interface ViewNegotiatorRegistry {

	/**
	 * The default negotiator is used when negotiation fails. If none is set, the request will fail with
	 * a 406.
	 * 
	 * @return
	 */
	public Negotiator<?> getDefaultNegotiator();

	/**
	 * Specify the {@link Negotiator} to use if negotiation fails. If not set (or set to null),
	 * failed negotiation wil result in a 406/NotAcceptable response.
	 * 
	 * @param negotiator
	 */
	public void setDefaultNegotiator(Negotiator<?> negotiator);

	public void addNegotiator(String contentType, Negotiator<?> negotiator);

	public void removeNegotiator(String contentType);

	public <V> Negotiator<V> getNegotiator(String contentType);

	public List<String> listNegotiatedContentTypes();

}
