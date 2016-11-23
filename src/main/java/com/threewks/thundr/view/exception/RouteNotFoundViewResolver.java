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
package com.threewks.thundr.view.exception;

import com.threewks.thundr.request.Request;
import com.threewks.thundr.request.Response;
import com.threewks.thundr.route.RouteNotFoundException;
import com.threewks.thundr.view.ViewResolver;

public class RouteNotFoundViewResolver implements ViewResolver<RouteNotFoundException> {
    private HttpStatusExceptionViewResolver delegate = new HttpStatusExceptionViewResolver();

    public RouteNotFoundViewResolver(HttpStatusExceptionViewResolver delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public void resolve(Request req, Response resp, RouteNotFoundException viewResult) {
        delegate.resolve(req, resp, viewResult);
    }

    @Override
    public String toString() {
        return this.getClass()
                   .getSimpleName();
    }
}
