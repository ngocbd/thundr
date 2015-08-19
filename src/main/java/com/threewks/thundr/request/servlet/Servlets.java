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
package com.threewks.thundr.request.servlet;

import org.joda.time.Duration;

import com.threewks.thundr.http.Cookie;

public class Servlets {
	public static Cookie toThundrCookie(javax.servlet.http.Cookie cookie) {
		// @formatter:off
		Duration expires = cookie.getMaxAge() > 0 ? Duration.standardSeconds(cookie.getMaxAge()) : null;
		return Cookie.build(cookie.getName())
				.withValue(cookie.getValue())
				.withComment(cookie.getComment())
				.withDomain(cookie.getDomain())
				.withMaxAge(expires)
				.withPath(cookie.getPath())
				.withSecure(cookie.getSecure())
				.withVersion(cookie.getVersion())
				.build();
		// @formatter:on
	}

	public static javax.servlet.http.Cookie toServletCookie(Cookie cookie) {
		javax.servlet.http.Cookie result = new javax.servlet.http.Cookie(cookie.getName(), cookie.getValue());
		if (cookie.getDomain() != null) {
			result.setDomain(cookie.getDomain());
		}
		if (cookie.getPath() != null) {
			result.setPath(cookie.getPath());
		}
		if (cookie.getMaxAge() != null) {
			result.setMaxAge((int) cookie.getMaxAge().getStandardSeconds());
		}
		if (cookie.getComment() != null) {
			result.setComment(cookie.getComment());
		}
		if (cookie.getVersion() != null) {
			result.setVersion(cookie.getVersion());
		}
		if (cookie.getSecure() != null) {
			result.setSecure(cookie.getSecure());
		}
		return result;
	}
}
