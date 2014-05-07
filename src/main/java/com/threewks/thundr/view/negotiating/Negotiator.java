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

import com.threewks.thundr.injection.Module;
import com.threewks.thundr.view.DataView;
import com.threewks.thundr.view.ViewResolver;

/**
 * A {@link Negotiator} is used to allow a {@link ViewResolver} to participate in content-negotiation using the {@link NegotiatingView}.
 * 
 * Negotiators are added to the {@link ViewNegotiatorRegistry} using {@link ViewNegotiatorRegistry#addNegotiator(String, Negotiator)} at application startup in your {@link Module}.
 * 
 * A {@link Negotiator} implementation should return the view object (usually a {@link DataView}) desired.
 */
public interface Negotiator<V> {
	public V create(NegotiatingView view);
}
