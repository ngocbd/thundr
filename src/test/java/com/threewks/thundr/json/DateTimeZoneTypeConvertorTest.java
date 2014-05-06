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
package com.threewks.thundr.json;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.joda.time.DateTimeZone;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class DateTimeZoneTypeConvertorTest {

	private DateTimeZoneTypeConvertor dateTimeZoneTypeConvertor = new DateTimeZoneTypeConvertor();

	@Test
	public void shouldSerializeTimezoneName() {
		DateTimeZone z = DateTimeZone.forID("Australia/Sydney");

		assertThat(dateTimeZoneTypeConvertor.serialize(z, null, null).toString(), is("\"Australia/Sydney\""));
	}

	@Test
	public void shouldSerializeTimezoneOffset() {
		DateTimeZone z = DateTimeZone.forOffsetHours(8);

		assertThat(dateTimeZoneTypeConvertor.serialize(z, null, null).toString(), is("\"+08:00\""));
	}

	@Test
	public void shouldDeserializeTimezoneName() {
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(DateTimeZone.class, new DateTimeZoneTypeConvertor());
		Gson gson = builder.create();

		DateTimeZone result = gson.fromJson("\"Australia/Sydney\"", DateTimeZone.class);
		assertThat(result, is(DateTimeZone.forID("Australia/Sydney")));
	}

	@Test
	public void shouldDeserializeTimezoneOffset() {
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(DateTimeZone.class, new DateTimeZoneTypeConvertor());
		Gson gson = builder.create();

		DateTimeZone result = gson.fromJson("\"+08:00\"", DateTimeZone.class);
		assertThat(result, is(DateTimeZone.forOffsetHours(8)));
	}
}
