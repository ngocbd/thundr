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
package com.threewks.thundr.http;

import org.apache.commons.lang3.StringUtils;

import com.threewks.thundr.request.Request;
import com.threewks.thundr.request.Response;
import com.threewks.thundr.util.Encoder;

public class Authorization {
	public static final String Basic = "Basic";
	public static final String Bearer = "Bearer";

	public static String createBasicHeader(String username, String password) {
		return Basic + " " + new Encoder(username + ":" + password).base64().string();
	}

	public static String createBearerHeader(String token) {
		return Bearer + " " + token;
	}

	public static void writeBearerHeader(Response resp, String token) {
		resp.withHeader(Header.Authorization, createBearerHeader(token));
	}

	public static String readBearerHeader(Request req) {
		String header = req.getHeader(Header.Authorization);
		return StringUtils.removeStartIgnoreCase(header, "Bearer ");
	}
}
