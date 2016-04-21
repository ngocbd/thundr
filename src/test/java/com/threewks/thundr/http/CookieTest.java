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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import org.joda.time.Duration;
import org.junit.Test;

import com.threewks.thundr.http.Cookie.CookieBuilder;

public class CookieTest {
	@Test
	public void shouldCreateCookieRetainingBasicValues() {
		Cookie cookie = new Cookie("name", "value");
		assertThat(cookie.getName(), is("name"));
		assertThat(cookie.getValue(), is("value"));
		assertThat(cookie.getComment(), is(nullValue()));
		assertThat(cookie.getDomain(), is(nullValue()));
		assertThat(cookie.getMaxAge(), is(nullValue()));
		assertThat(cookie.getPath(), is("/"));
		assertThat(cookie.getSecure(), is(nullValue()));
		assertThat(cookie.getVersion(), is(nullValue()));
	}

	@Test
	public void shouldCreateCookieRetainingAllValues() {
		Cookie cookie = new Cookie("name", "value", "/path/", "domain.com", Duration.millis(10), "Comment", 1, true);
		assertThat(cookie.getName(), is("name"));
		assertThat(cookie.getValue(), is("value"));
		assertThat(cookie.getComment(), is("Comment"));
		assertThat(cookie.getDomain(), is("domain.com"));
		assertThat(cookie.getMaxAge(), is(Duration.millis(10)));
		assertThat(cookie.getPath(), is("/path/"));
		assertThat(cookie.getSecure(), is(true));
		assertThat(cookie.getVersion(), is(1));
	}

	@Test
	public void shouldBuildABasicCookie() {
		Cookie cookie = CookieBuilder.build("name").withValue("value").build();
		assertThat(cookie.getName(), is("name"));
		assertThat(cookie.getValue(), is("value"));
		assertThat(cookie.getVersion(), is((Integer) null));
		assertThat(cookie.getComment(), is(nullValue()));
		assertThat(cookie.getDomain(), is(nullValue()));
		assertThat(cookie.getPath(), is("/"));
		assertThat(cookie.getSecure(), is((Boolean) null));
		assertThat(cookie.getMaxAge(), is((Duration) null));
	}

	@Test
	public void shouldHaveDefaultCtorAndWithName() {
		Cookie cookie = new CookieBuilder().withName("name").build();
		assertThat(cookie.getName(), is("name"));
	}

	@Test
	public void shouldBuildAFullCookie() {
		// @formatter:off
		CookieBuilder builder = CookieBuilder.build("name")
			.withValue("value")
			.withDomain("domain.com")
			.withPath("/path/.")
			.withSecure(true)
			.withMaxAge(new Duration(1234))
			.withVersion(2)
			.withComment("Comment");
		// @formatter:on
		Cookie cookie = builder.build();
		assertThat(cookie.getName(), is("name"));
		assertThat(cookie.getValue(), is("value"));
		assertThat(cookie.getVersion(), is(2));
		assertThat(cookie.getComment(), is("Comment"));
		assertThat(cookie.getDomain(), is("domain.com"));
		assertThat(cookie.getPath(), is("/path/."));
		assertThat(cookie.getSecure(), is(true));
	}

	@Test
	public void shouldProvideImmutableBuilder() {
		CookieBuilder builder = CookieBuilder.build("name");
		assertThat(builder.withValue("value"), is(not(sameInstance(builder))));
		assertThat(builder.withDomain("domain.com"), is(not(sameInstance(builder))));
		assertThat(builder.withPath("/path/."), is(not(sameInstance(builder))));
		assertThat(builder.withSecure(true), is(not(sameInstance(builder))));
		assertThat(builder.withMaxAge(new Duration(1234)), is(not(sameInstance(builder))));
		assertThat(builder.withVersion(2), is(not(sameInstance(builder))));
		assertThat(builder.withComment("Comment"), is(not(sameInstance(builder))));
	}

	@Test
	public void shouldHaveUsefulToString() {
		CookieBuilder builder = CookieBuilder.build("name");
		assertThat(builder.toString(), is("name=null (/);"));

		builder = builder.withValue("value");
		assertThat(builder.toString(), is("name=value (/);"));

		builder = builder.withDomain("domain.com");
		builder = builder.withPath("/path/.");

		assertThat(builder.toString(), is("name=value (domain.com/path/.);"));

		builder = builder.withSecure(true);

		assertThat(builder.toString(), is("name=value [secure] (domain.com/path/.);"));

		builder = builder.withMaxAge(new Duration(30000));
		builder = builder.withVersion(2);
		builder = builder.withComment("Comment");

		assertThat(builder.toString(), is("name=value [secure] (domain.com/path/.) expires 30s v2;"));
	}

}
