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
package com.threewks.thundr.view;

import java.util.HashMap;
import java.util.Map;

/**
 * A base class for 'template' views. Template views combine data with a template to produce an output. Examples are
 * jsps, handlebars etc.
 * They usually consist of a template and a data model which contains stateful data and reference data.
 * 
 * @param <Self>
 */
public abstract class TemplateView<Self extends TemplateView<Self>> extends BaseView<Self> {
	protected String view;
	protected Map<String, Object> model;

	public TemplateView(String view) {
		this(view, new HashMap<String, Object>());
	}

	public TemplateView(String view, Map<String, Object> model) {
		super();
		this.view = view;
		this.model = model;
	}

	public String getView() {
		return view;
	}

	public Map<String, Object> getModel() {
		return model;
	}
}
