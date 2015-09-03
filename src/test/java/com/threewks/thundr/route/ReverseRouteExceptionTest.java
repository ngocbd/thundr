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

public class ReverseRouteExceptionTest {

	@Test
	public void shouldRetainMessage() {
		assertThat(new ReverseRouteException("For%mat").getMessage(), is("For%mat"));
		assertThat(new ReverseRouteException("For%smat", " ").getMessage(), is("For mat"));
	}

	@Test
	public void shouldCauseAndRetainMessage() {
		Exception e = new Exception();
		assertThat(new ReverseRouteException(e, "For%mat").getMessage(), is("For%mat"));
		assertThat(new ReverseRouteException(e, "For%mat").getCause(), is((Throwable) e));
		;
		assertThat(new ReverseRouteException(e, "For%smat", " ").getMessage(), is("For mat"));
		assertThat(new ReverseRouteException(e, "For%smat", " ").getCause(), is((Throwable) e));
		;
	}
}
