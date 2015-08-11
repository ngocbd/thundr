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
package com.threewks.thundr.view.jsp;

import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.threewks.thundr.exception.BaseException;
import com.threewks.thundr.request.Request;
import com.threewks.thundr.request.Response;
import com.threewks.thundr.view.BaseView;
import com.threewks.thundr.view.GlobalModel;
import com.threewks.thundr.view.ViewResolutionException;
import com.threewks.thundr.view.ViewResolver;

public class JspViewResolver implements ViewResolver<JspView> {
	/**
	 * These values will be available in the model for every jsp resolved by this resolver.
	 * An individual JspView will override the values in this map if it contains an entry with the same key.
	 * 
	 * This can be useful for setting values that apply to every page, or are extremely common. For example,
	 * application environment, domain or version number.
	 */
	protected GlobalModel globalModel;

	public JspViewResolver(GlobalModel globalModel) {
		this.globalModel = globalModel;
	}

	@Override
	public void resolve(Request request, Response response, JspView viewResult) {
		try {
			HttpServletRequest req = request.getRawRequest(HttpServletRequest.class);
			HttpServletResponse resp = response.getRawResponse(HttpServletResponse.class);
			
			// different containers handle missing resources differently when you include content, we perform this
			// check up front to help normalise their behaviour
			if (req.getSession().getServletContext().getResource(viewResult.getView()) == null) {
				throw new BaseException("resource %s does not exist", viewResult.getView());
			}
			String url = resp.encodeRedirectURL(viewResult.getView());
			includeModelInRequest(req, globalModel);
			includeModelInRequest(req, viewResult.getModel());
			BaseView.applyToResponse(viewResult, response);
			RequestDispatcher requestDispatcher = req.getRequestDispatcher(url);
			requestDispatcher.include(req, resp);
		} catch (Exception e) {
			throw new ViewResolutionException(e, "Failed to resolve JSP view %s - %s", viewResult, e.getMessage());
		}
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
	

	public static void includeModelInRequest(HttpServletRequest req, Map<String, Object> model) {
		for (Map.Entry<String, Object> modelEntry : model.entrySet()) {
			req.setAttribute(modelEntry.getKey(), modelEntry.getValue());
		}
	}

}
