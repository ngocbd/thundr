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
package com.threewks.thundr.view.negotiating;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.atomicleopard.expressive.Expressive;

public class ViewNegotiatorRegistryImpl implements ViewNegotiatorRegistry {
	private Map<String, Negotiator<?>> negotiatorsByContentType = new LinkedHashMap<String, Negotiator<?>>();
	private Negotiator<?> defaultNegotiator = null;

	@Override
	public void addNegotiator(String contentType, Negotiator<?> negotiator) {
		this.negotiatorsByContentType.put(normaliseContentType(contentType), negotiator);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <V> Negotiator<V> getNegotiator(String contentType) {
		return (Negotiator<V>) this.negotiatorsByContentType.get(normaliseContentType(contentType));
	}

	@Override
	public void removeNegotiator(String contentType) {
		this.negotiatorsByContentType.remove(normaliseContentType(contentType));
	}

	@Override
	public List<String> listNegotiatedContentTypes() {
		return Expressive.list(this.negotiatorsByContentType.keySet()).sort(Expressive.Comparators.caseInsensitive());
	}

	@Override
	public Negotiator<?> getDefaultNegotiator() {
		return defaultNegotiator;
	}

	@Override
	public void setDefaultNegotiator(Negotiator<?> negotiator) {
		this.defaultNegotiator = negotiator;
	}

	public static String normaliseContentType(String contentType) {
		contentType = StringUtils.lowerCase(StringUtils.trimToEmpty(contentType));
		return contentType;
	}
}
