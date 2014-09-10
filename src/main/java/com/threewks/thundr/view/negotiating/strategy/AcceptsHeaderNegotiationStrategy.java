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
package com.threewks.thundr.view.negotiating.strategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import com.atomicleopard.expressive.Expressive;
import com.threewks.thundr.http.Header;
import com.threewks.thundr.view.negotiating.NegotiatingView;
import com.threewks.thundr.view.negotiating.Negotiator;
import com.threewks.thundr.view.negotiating.ViewNegotiatorRegistry;
import com.threewks.thundr.view.negotiating.ViewNegotiatorRegistryImpl;

/**
 * Finds a {@link Negotiator} by looking at the Accept header provided on the request.
 * 
 * RFC: <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html">http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html</a>
 */
public class AcceptsHeaderNegotiationStrategy implements NegotiationStrategy {

	@Override
	public Negotiator<?> findNegotiator(HttpServletRequest req, NegotiatingView view, ViewNegotiatorRegistry viewNegotiatorRegistry) {
		String acceptsHeader = Header.getHeader(Header.Accept, req);
		if (StringUtils.isNotBlank(acceptsHeader)) {
			List<AcceptsComponent> orderedAcceptsHeader = cleanAndOrderAcceptHeader(acceptsHeader);
			for (AcceptsComponent acceptsComponent : orderedAcceptsHeader) {
				String accept = acceptsComponent.getAccept();
				Negotiator<?> negotiator = viewNegotiatorRegistry.getNegotiator(accept);
				if (negotiator != null) {
					return negotiator;
				}
			}
		}
		return null;
	}

	private List<AcceptsComponent> cleanAndOrderAcceptHeader(String acceptsHeader) {
		List<AcceptsComponent> components = new ArrayList<AcceptsComponent>();
		for (String component : StringUtils.split(acceptsHeader, ",")) {
			components.add(new AcceptsComponent(ViewNegotiatorRegistryImpl.normaliseContentType(component)));
		}
		Collections.sort(components, AcceptsComponent.Comparator);
		return components;
	}

	protected static class AcceptsComponent {
		// This is a broad pattern to roughly match ;q=1 ;q=.5 and q=0.5 - it will also potentially match q= and q=. though.
		private static final Pattern QualityPattern = Pattern.compile("^(.+?);q=([0-9]*\\.?[0-9]+)");
		private String accept;
		private float quality;
		private int components;

		public AcceptsComponent(String component) {
			super();
			component = StringUtils.trim(component);
			this.quality = 1;
			Matcher matcher = QualityPattern.matcher(component);
			if (matcher.matches()) {
				this.accept = matcher.group(1);
				this.quality = Float.parseFloat(matcher.group(2));
			} else {
				this.accept = component;
			}
			this.components = StringUtils.countMatches(accept, ";") + 1;
		}

		public float getQuality() {
			return quality;
		}

		public int getComponents() {
			return components;
		}

		public String getAccept() {
			return accept;
		}

		@Override
		public String toString() {
			return accept;
		}

		public static final Comparator<AcceptsComponent> Comparator = Expressive.Comparators.compare(AcceptsComponent.class).on("quality").using(Collections.reverseOrder()).on("components")
				.using(Collections.reverseOrder());
	}
}
