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
package com.threewks.thundr.route;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class ReverseRouteTest {

	@Test
	public void shouldRetainFields() {
		ReverseRoute reverseRoute = new ReverseRoute(HttpMethod.HEAD, "/uri", "rel");
		assertThat(reverseRoute.getMethod(), is(HttpMethod.HEAD));
		assertThat(reverseRoute.getUri(), is("/uri"));
		assertThat(reverseRoute.getRel(), is("rel"));
	}
	
	@Test
	public void shouldHaveSensibleToString() {
		assertThat(new ReverseRoute(HttpMethod.HEAD, "/uri", "rel").toString(), is("(rel) HEAD /uri"));
		assertThat(new ReverseRoute(HttpMethod.GET, "/uri", null).toString(), is("GET /uri"));
	}
	
	@Test
	public void shouldHaveEquality(){
		ReverseRoute a = new ReverseRoute(HttpMethod.HEAD, "/uri", "rel");
		ReverseRoute b = new ReverseRoute(HttpMethod.HEAD, "/uri", "rel");
		ReverseRoute c = new ReverseRoute(HttpMethod.GET, "/uri", "rel");
		ReverseRoute d = new ReverseRoute(HttpMethod.HEAD, "/uri2", "rel");
		ReverseRoute e = new ReverseRoute(HttpMethod.HEAD, "/uri", null);
		
		assertThat(a.equals(a), is(true));
		assertThat(a.equals(b), is(true));
		assertThat(b.equals(a), is(true));
		assertThat(a.equals(c), is(false));
		assertThat(a.equals(d), is(false));
		assertThat(a.equals(e), is(false));
		assertThat(e.equals(a), is(false));
		assertThat(a.equals(null), is(false));
		assertThat(a.equals("a string"), is(false));
		
		assertThat(a.hashCode() == b.hashCode(), is(true));
		assertThat(a.hashCode() == c.hashCode(), is(false));
		assertThat(a.hashCode() == d.hashCode(), is(false));
		assertThat(a.hashCode() == e.hashCode(), is(false));
	}
}
