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
package com.threewks.thundr.json;

import java.lang.reflect.Type;

import org.joda.time.DateTimeZone;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class DateTimeZoneTypeConvertor implements JsonSerializer<DateTimeZone>, JsonDeserializer<DateTimeZone> {
	@Override
	public JsonElement serialize(DateTimeZone src, Type srcType, JsonSerializationContext context) {
		return new JsonPrimitive(src.getID());
	}

	@Override
	public DateTimeZone deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
		return DateTimeZone.forID(json.getAsString());
	}
}