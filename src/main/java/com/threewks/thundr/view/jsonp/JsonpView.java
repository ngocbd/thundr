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
package com.threewks.thundr.view.jsonp;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.threewks.thundr.view.DataView;
import com.threewks.thundr.view.View;

import jodd.util.MimeTypes;
import jodd.util.StringPool;

public class JsonpView extends DataView<JsonpView> implements View {
	public JsonpView(Object output) {
		super(output);
		applyDefaults();
	}

	public JsonpView(DataView<?> other) {
		super(other);
		applyDefaults();
	}
	
	private void applyDefaults() {
		if (StringUtils.isBlank(getContentType())) {
			withContentType(MimeTypes.MIME_APPLICATION_JAVASCRIPT);
		}
		if (StringUtils.isBlank(getCharacterEncoding())) {
			withCharacterEncoding(StringPool.UTF_8);
		}
		if (getStatusCode() == null) {
			withStatusCode(HttpServletResponse.SC_OK);
		}
	}
}
