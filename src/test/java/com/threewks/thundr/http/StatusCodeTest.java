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

import static com.threewks.thundr.http.StatusCode.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class StatusCodeTest {

	@Test
	public void shouldReturnCodeAndReasonForStatusCode() {
		assertThat(StatusCode.OK.getCode(), is(200));
		assertThat(StatusCode.OK.getReason(), is("OK"));

		for (StatusCode code : StatusCode.values()) {
			assertThat(code.getCode(), greaterThan(0));
			assertThat(code.getReason(), is(notNullValue()));
		}
	}

	@Test
	public void shouldReturnAStatusCodeForAValue() {
		assertThat(StatusCode.fromCode(200), is(StatusCode.OK));

		for (StatusCode statusCode : StatusCode.values()) {
			assertThat(StatusCode.fromCode(statusCode.getCode()), is(statusCode));
		}
	}

	@Test
	public void shouldReturnToStringOfCodeAndReason() {
		assertThat(StatusCode.OK.toString(), is("200 OK"));

		for (StatusCode statusCode : StatusCode.values()) {
			assertThat(statusCode.toString(), is(statusCode.getCode() + " " + statusCode.getReason()));
		}
	}

	@Test
	public void shouldReturnTrueWhenInRange() {
		assertThat(StatusCode.Continue.isInRange(100, 199), is(true));
		assertThat(StatusCode.Continue.isInRange(101, 199), is(false));
		assertThat(StatusCode.Continue.isInRange(100, 101), is(true));
		assertThat(StatusCode.Continue.isInRange(100, 100), is(false));

		assertThat(StatusCode.OK.isInRange(200, 299), is(true));
		assertThat(StatusCode.OK.isInRange(201, 299), is(false));
		assertThat(StatusCode.OK.isInRange(200, 201), is(true));
		assertThat(StatusCode.OK.isInRange(100, 200), is(false));
		assertThat(StatusCode.OK.isInRange(200, 200), is(false));
		assertThat(StatusCode.OK.isInRange(0, 200), is(false));
		assertThat(StatusCode.OK.isInRange(0, 201), is(true));
	}

	@Test
	public void shouldReturnTrueWhenInRangeUsingStatusCode() {
		assertThat(StatusCode.Continue.isInRange(null, OK), is(false));
		assertThat(StatusCode.Continue.isInRange(OK, null), is(false));
		assertThat(StatusCode.Continue.isInRange(Continue, OK), is(true));
		assertThat(StatusCode.Continue.isInRange(SwitchingProtocols, OK), is(false));

		assertThat(StatusCode.OK.isInRange(OK, Accepted), is(true));
		assertThat(StatusCode.OK.isInRange(Created, Accepted), is(false));
		assertThat(StatusCode.OK.isInRange(Continue, SwitchingProtocols), is(false));
	}

	@Test
	public void shouldReturnTrueWhenInFamily() {
		assertThat(StatusCode.OK.isInFamily(null), is(false));
		assertThat(StatusCode.OK.isInFamily(OK), is(true));
		assertThat(StatusCode.Accepted.isInFamily(OK), is(true));
		assertThat(StatusCode.OK.isInFamily(Accepted), is(true));
		assertThat(StatusCode.OK.isInFamily(BadRequest), is(false));
		assertThat(StatusCode.BadRequest.isInFamily(OK), is(false));
		assertThat(StatusCode.NotFound.isInFamily(BadRequest), is(true));
		assertThat(StatusCode.BadRequest.isInFamily(NotFound), is(true));
		assertThat(StatusCode.Found.isInFamily(PermanentRedirect), is(true));
		assertThat(StatusCode.PermanentRedirect.isInFamily(Found), is(true));
		assertThat(StatusCode.PermanentRedirect.isInFamily(NotFound), is(false));
		assertThat(StatusCode.PermanentRedirect.isInFamily(OK), is(false));
	}

	@Test
	public void shouldReturnTrueWhenInNumericFamily() {
		assertThat(StatusCode.OK.isInFamily(200), is(true));
		assertThat(StatusCode.Accepted.isInFamily(202), is(true));
		assertThat(StatusCode.OK.isInFamily(200), is(true));
		assertThat(StatusCode.OK.isInFamily(400), is(false));
		assertThat(StatusCode.BadRequest.isInFamily(201), is(false));
		assertThat(StatusCode.NotFound.isInFamily(400), is(true));
		assertThat(StatusCode.BadRequest.isInFamily(404), is(true));
		assertThat(StatusCode.TemporaryRedirect.isInFamily(302), is(true));
		assertThat(StatusCode.PermanentRedirect.isInFamily(301), is(true));
		assertThat(StatusCode.PermanentRedirect.isInFamily(404), is(false));
		assertThat(StatusCode.PermanentRedirect.isInFamily(200), is(false));
	}

}
