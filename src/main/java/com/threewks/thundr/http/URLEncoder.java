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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.atomicleopard.expressive.ETransformer;
import com.threewks.thundr.exception.BaseException;
import com.threewks.thundr.transformer.TransformerManager;

/**
 * Encodes URL elements as needed.
 * 
 * see http://tools.ietf.org/html/rfc2396 for details.
 */
public class URLEncoder {
	private static final String unreserved = "a-zA-Z0-9";
	private static final String mark = "_!~*'()\\.\\-";
	private static final Pattern acceptableQueryCharacters = Pattern.compile("[" + unreserved + mark + "]*");
	private static final Pattern acceptablePathCharacters = Pattern.compile("[" + unreserved + mark + "]*");

	/**
	 * Encodes the given value to be used as a URL/URI path component.
	 */
	public static final String encodePathComponent(String value) {
		return escapeUsingPattern(acceptablePathCharacters, value);
	}

	/**
	 * Encodes the given value to be used as a URL/URI path slug - that is it removes non-alpha characters and introduces dashes.
	 * 
	 * @param value
	 * @return
	 */
	public static final String encodePathSlugComponent(String value) {
		return value == null ? value : value.replaceAll("'", "").replaceAll("\\W", "-").replaceAll("-+", "-");
	}

	/**
	 * Encodes the given value to be used as a URL/URI query component.
	 */
	public static final String encodeQueryComponent(String value) {
		return escapeUsingPattern(acceptableQueryCharacters, value);
	}

	/**
	 * Encodes the given query parameters into the query string such that it returns &lt;code&gt;?param1=value1&amp;param2=value2&amp;....&lt;/code&gt;.
	 * 
	 * Uses toString on parameters to determine their string representation.
	 * Returns an empty string if no parameters are provided.
	 * 
	 * @param parameters
	 * @return
	 */
	public static final String encodeQueryString(Map<String, Object> parameters) {
		if (parameters == null) {
			parameters = Collections.emptyMap();
		}
		List<String> fragments = new ArrayList<String>(parameters.size());
		for (Map.Entry<String, Object> entry : parameters.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue() == null ? "" : entry.getValue().toString();
			fragments.add(encodeQueryComponent(key) + "=" + encodeQueryComponent(value));
		}
		return fragments.isEmpty() ? "" : "?" + StringUtils.join(fragments, "&");
	}

	/**
	 * Encodes the given query parameters into the query string such that it returns &lt;code&gt;?param1=value1&amp;param2=value2&amp;....&lt;/code&gt;.
	 * 
	 * Uses the given {@link TransformerManager} on parameters to determine their string representation.
	 * Returns an empty string if no parameters are provided.
	 * 
	 * @param parameters
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static final String encodeQueryString(Map<String, Object> parameters, TransformerManager transformerManager) {
		if (parameters == null) {
			parameters = Collections.emptyMap();
		}
		Map<String, Object> delegate = new LinkedHashMap<>();
		for (Map.Entry<String, Object> entry : parameters.entrySet()) {
			Object valueObj = entry.getValue();
			String value = "";
			if (valueObj != null) {
				Class<Object> type = (Class<Object>) entry.getValue().getClass();
				ETransformer<Object, String> transformer = transformerManager.getTransformerSafe(type, String.class);
				value = transformer.from(entry.getValue());
			}
			delegate.put(entry.getKey(), value);
		}
		return encodeQueryString(delegate);
	}

	/**
	 * Decodes the given query parameter string into key value pairs.
	 * 
	 * If the given string begins with '?', it will be stripped off. Pairs are decoded before being returned.
	 * 
	 * @param query
	 * @return
	 */
	public static Map<String, List<String>> decodeQueryString(String query) {
		query = StringUtils.trimToEmpty(query);
		query = StringUtils.removeStart(query, "?");
		Map<String, List<String>> results = new LinkedHashMap<>();
		if (StringUtils.isNotBlank(query)) {
			for (String pair : query.split("&")) {
				String[] parts = StringUtils.split(pair, "=", 2);
				String key = unescape(parts[0]);
				String value = parts.length > 1 ? unescape(parts[1]) : null;
				List<String> existing = results.get(key);
				if (existing == null) {
					existing = new ArrayList<>();
					results.put(key, existing);
				}
				existing.add(value);
			}
		}
		return results;
	}

	public static final String decodePathComponent(String value) {
		return unescape(value);
	}

	private static String unescape(String value) {
		try {
			return URLDecoder.decode(value, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new BaseException(e);
		}
	}

	private static String escapeUsingPattern(Pattern pattern, String value) {
		if (value == null || value.length() == 0 || pattern.matcher(value).matches()) {
			return value;
		}
		StringBuilder output = new StringBuilder();
		for (int i = 0; i < value.length(); i++) {
			String character = value.substring(i, i + 1);

			if (!pattern.matcher(character).matches()) {
				output.append("%");
				output.append(Integer.toHexString((int) character.charAt(0)).toUpperCase());
			} else {
				output.append(character);
			}
		}
		return output.toString();
	}
}