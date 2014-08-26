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
package com.threewks.thundr.route.rewrite;

import static com.atomicleopard.expressive.Expressive.map;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.Map;

import org.junit.Test;

import com.threewks.thundr.route.rewrite.Rewrite;

public class RewriteActionTest {
	private Map<String, String> emptyPathVars = Collections.emptyMap();

	@Test
	public void shouldCreateSimpleRedirect() {
		Rewrite redirectAction = new Rewrite("/path/to/file.html");
		assertThat(redirectAction.getRewriteTo(emptyPathVars), is("/path/to/file.html"));
	}

	@Test
	public void shouldRedirectWithPathVar() {
		Map<String, String> pathVars = map("to", "destination");
		Rewrite redirectAction = new Rewrite("/path/{to}/file.html");
		assertThat(redirectAction.getRewriteTo(pathVars), is("/path/destination/file.html"));
	}

	@Test
	public void shouldRedirectWithMissingPathVar() {
		Map<String, String> pathVars = map("to", "destination");
		Rewrite redirectAction = new Rewrite("/path/{to}/{file}.html");
		assertThat(redirectAction.getRewriteTo(pathVars), is("/path/destination/.html"));
	}

	@Test
	public void shouldRedirectWithExtraPathVars() {
		Map<String, String> pathVars = map("to", "destination", "other", "whatever");
		Rewrite redirectAction = new Rewrite("/path/{to}/other");
		assertThat(redirectAction.getRewriteTo(pathVars), is("/path/destination/other"));
	}

	@Test
	public void shouldHaveUsefulToString() {
		assertThat(new Rewrite("/path/{to}").toString(), is("Rewrite:/path/{to}"));
	}
}
