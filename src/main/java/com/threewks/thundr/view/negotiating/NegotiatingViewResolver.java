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
package com.threewks.thundr.view.negotiating;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.threewks.thundr.http.StatusCode;
import com.threewks.thundr.injection.Module;
import com.threewks.thundr.request.Request;
import com.threewks.thundr.request.Response;
import com.threewks.thundr.view.View;
import com.threewks.thundr.view.ViewResolver;
import com.threewks.thundr.view.ViewResolverRegistry;
import com.threewks.thundr.view.negotiating.strategy.AcceptsHeaderNegotiationStrategy;
import com.threewks.thundr.view.negotiating.strategy.ContentTypeNegotiationStrategy;
import com.threewks.thundr.view.negotiating.strategy.DefaultNegotiatorNegotiationStrategy;
import com.threewks.thundr.view.negotiating.strategy.FileExtensionNegotiationStrategy;
import com.threewks.thundr.view.negotiating.strategy.NegotiationStrategy;

/**
 * This view resolver performs content negotiation.
 * It determines the final output format (json, xml etc) based on the client request.
 * 
 * It finds a delegate {@link View} implementation using the {@link ViewNegotiatorRegistry}, and then
 * invokes the {@link ViewResolver} for that view.
 * 
 * First, a content type is derived based on the request using the registered {@link NegotiationStrategy} instances.
 * These are added using {@link #addNegotiationStrategy(NegotiationStrategy)} add application startup (in your {@link Module}).
 * 
 * This content type is then mapped to a {@link Negotiator} using the {@link ViewNegotiatorRegistry}.
 * 
 * Finally, the {@link Negotiator} provides a view implementation, which is resolved as normal using the {@link ViewResolverRegistry}.
 * 
 * By default, the content type is determined using the following strategies (in order):
 * - If a content type has been explicitly set, it will be used (i.e. {@link NegotiatingView#withContentType(String)}) - {@link ContentTypeNegotiationStrategy} - If the request has a file extension
 * which can be mapped to a content type, it will be used (i.e. /path/file.xml ) - {@link FileExtensionNegotiationStrategy} - If the client send an Accepts header, content negotiation will occur based
 * on the given mime types and their quality - {@link AcceptsHeaderNegotiationStrategy} - If a default {@link Negotiator} has been set on the {@link ViewNegotiatorRegistry}, it will be used.
 * 
 * If you add further any more {@link NegotiationStrategy} instances, they will take priority, last added being the most important
 * 
 * If negotiation fails, 406 (Not Acceptable) will be returned.
 */
public class NegotiatingViewResolver implements ViewResolver<NegotiatingView> {
	protected ViewResolverRegistry viewResolverRegistry;
	protected ViewNegotiatorRegistry viewNegotiatorRegistry;
	private Map<Class<? extends NegotiationStrategy>, NegotiationStrategy> negotiationStrategies = new HashMap<Class<? extends NegotiationStrategy>, NegotiationStrategy>();
	private List<NegotiationStrategy> orderedNegotiationStrategies = new ArrayList<NegotiationStrategy>();

	public NegotiatingViewResolver(ViewResolverRegistry viewResolverRegistry, ViewNegotiatorRegistry viewNegotiatorRegistry) {
		this.viewResolverRegistry = viewResolverRegistry;
		this.viewNegotiatorRegistry = viewNegotiatorRegistry;
		registerDefaultNegotiationStrategies();
	}

	@Override
	public void resolve(Request req, Response resp, NegotiatingView viewResult) {
		Object negotiatedView = determineView(req, viewResult);
		ViewResolver<Object> viewResolver = viewResolverRegistry.findViewResolver(negotiatedView);
		if (viewResolver == null) {
			// send not acceptable
			resp
				.withStatusCode(StatusCode.NotAcceptable)
				.withStatusMessage("Unable to match any requested content types in the Accept header.")
				.finaliseHeaders();
		} else {
			viewResolver.resolve(req, resp, negotiatedView);
		}
	}

	public void addNegotiationStrategy(NegotiationStrategy strategy) {
		this.negotiationStrategies.put(strategy.getClass(), strategy);
		this.orderedNegotiationStrategies.add(0, strategy);
	}

	public void removeNegotiationStrategy(Class<? extends NegotiationStrategy> strategy) {
		NegotiationStrategy instance = this.negotiationStrategies.remove(strategy);
		this.orderedNegotiationStrategies.remove(instance);
	}

	public List<NegotiationStrategy> getNegotiationStrategies() {
		return Collections.unmodifiableList(orderedNegotiationStrategies);
	}

	protected void registerDefaultNegotiationStrategies() {
		// these are iterated in reverse order, so put the most general ones first.
		addNegotiationStrategy(new DefaultNegotiatorNegotiationStrategy());
		addNegotiationStrategy(new AcceptsHeaderNegotiationStrategy());
		addNegotiationStrategy(new FileExtensionNegotiationStrategy());
		addNegotiationStrategy(new ContentTypeNegotiationStrategy());
	}

	protected Object determineView(Request req, NegotiatingView negotiatedView) {
		Negotiator<?> negotiator = findNegotiator(req, negotiatedView);
		return negotiator == null ? null : negotiator.create(negotiatedView);
	}

	protected Negotiator<?> findNegotiator(Request req, NegotiatingView negotiatedView) {
		for (NegotiationStrategy negotiationStrategy : orderedNegotiationStrategies) {
			Negotiator<?> negotiator = negotiationStrategy.findNegotiator(req, negotiatedView, viewNegotiatorRegistry);
			if (negotiator != null) {
				return negotiator;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
}
