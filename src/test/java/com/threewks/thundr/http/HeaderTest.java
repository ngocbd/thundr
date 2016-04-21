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
package com.threewks.thundr.http;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class HeaderTest {

	@Test
	public void shouldNotBreakHeaderValuesByAccident() {
		assertThat(Header.Accept, is("Accept"));
		assertThat(Header.AcceptCharset, is("Accept-Charset"));
		assertThat(Header.AccessControlAllowCredentials, is("Access-Control-Allow-Credentials"));
		assertThat(Header.AccessControlAllowHeaders, is("Access-Control-Allow-Headers"));
		assertThat(Header.AccessControlAllowMethods, is("Access-Control-Allow-Methods"));
		assertThat(Header.AccessControlAllowOrigin, is("Access-Control-Allow-Origin"));
		assertThat(Header.AccessControlExposeHeaders, is("Access-Control-Expose-Headers"));
		assertThat(Header.AccessControlRequestMethod, is("Access-Control-Request-Method"));
		assertThat(Header.AccessControlRequestHeaders, is("Access-Control-Request-Headers"));
		assertThat(Header.AcceptEncoding, is("Accept-Encoding"));
		assertThat(Header.Authorization, is("Authorization"));
		assertThat(Header.CacheControl, is("Cache-Control"));
		assertThat(Header.ContentDisposition, is("Content-Disposition"));
		assertThat(Header.ContentEncoding, is("Content-Encoding"));
		assertThat(Header.ContentLength, is("Content-Length"));
		assertThat(Header.ContentTransferEncoding, is("Content-Transfer-Encoding"));
		assertThat(Header.ContentType, is("Content-Type"));
		assertThat(Header.Expires, is("Expires"));
		assertThat(Header.IfModifiedSince, is("If-Modified-Since"));
		assertThat(Header.LastModified, is("Last-Modified"));
		assertThat(Header.Location, is("Location"));
		assertThat(Header.Origin, is("Origin"));
		assertThat(Header.Pragma, is("Pragma"));
		assertThat(Header.SetCookie, is("Set-Cookie"));
		assertThat(Header.SetCookie2, is("Set-Cookie2"));
		assertThat(Header.UserAgent, is("User-Agent"));
		assertThat(Header.Vary, is("Vary"));
		assertThat(Header.XHttpMethodOverride, is("X-HTTP-Method-Override"));
		assertThat(Header.XXsrfToken, is("X-XSRF-TOKEN"));
	}
}
