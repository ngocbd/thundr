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

import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.Duration;

public class Cookie {
	protected String name;
	protected String value;
	protected String path;
	protected String domain;
	protected Duration maxAge;
	protected String comment;
	protected Integer version;
	protected Boolean secure;

	public Cookie(String name, String value) {
		this(name, value, null, null, null, null, null, null);
	}

	protected Cookie() {
		super();
	}

	public Cookie(String name, String value, String path, String domain, Duration maxAge, String comment, Integer version, Boolean secure) {
		super();
		this.name = name;
		this.value = value;
		this.path = path;
		this.domain = domain;
		this.maxAge = maxAge == null ? null : new Duration(maxAge);
		this.comment = comment;
		this.version = version;
		this.secure = secure;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	public String getPath() {
		return path;
	}

	public String getDomain() {
		return domain;
	}

	public Duration getMaxAge() {
		return maxAge;
	}

	public String getComment() {
		return comment;
	}

	public Integer getVersion() {
		return version;
	}

	public Boolean getSecure() {
		return secure;
	}

	@Override
	public String toString() {
		String domainAndPath = trimToEmpty(domain) + trimToEmpty(path);
		domainAndPath = StringUtils.isEmpty(domainAndPath) ? "" : " (" + domainAndPath + ")";
		String expires = this.maxAge == null ? "" : " expires " + this.maxAge.getStandardSeconds() + "s";
		return String.format("%s=%s%s%s%s%s;", name, value, Boolean.TRUE.equals(secure) ? " [secure]" : "", domainAndPath, expires, version == null ? "" : " v" + version, trimToEmpty(comment));
	}

	/**
	 * Creates a CookieBuilder, which provides a fluent api for building a {@link Cookie}.
	 * 
	 * @param name
	 * @return
	 */
	public static final CookieBuilder build(String name) {
		return new CookieBuilder(name);
	}

	public static class CookieBuilder extends Cookie {
		public CookieBuilder() {

		}

		public CookieBuilder(String name, String value, String path, String domain, Duration maxAge, String comment, Integer version, Boolean secure) {
			super(name, value, path, domain, maxAge, comment, version, secure);
		}

		public CookieBuilder(String name) {
			this.name = name;
		}

		public CookieBuilder withName(String name) {
			return new CookieBuilder(name, value, path, domain, maxAge, comment, version, secure);
		}

		public CookieBuilder withValue(String value) {
			return new CookieBuilder(name, value, path, domain, maxAge, comment, version, secure);
		}

		public CookieBuilder withPath(String path) {
			return new CookieBuilder(name, value, path, domain, maxAge, comment, version, secure);
		}

		public CookieBuilder withDomain(String domain) {
			return new CookieBuilder(name, value, path, domain, maxAge, comment, version, secure);
		}

		public CookieBuilder withMaxAge(Duration maxAge) {
			return new CookieBuilder(name, value, path, domain, maxAge, comment, version, secure);
		}

		public CookieBuilder withVersion(Integer version) {
			return new CookieBuilder(name, value, path, domain, maxAge, comment, version, secure);
		}

		public CookieBuilder withSecure(Boolean secure) {
			return new CookieBuilder(name, value, path, domain, maxAge, comment, version, secure);
		}

		public CookieBuilder withComment(String comment) {
			return new CookieBuilder(name, value, path, domain, maxAge, comment, version, secure);
		}

		public Cookie build() {
			return new Cookie(name, value, path, domain, maxAge, comment, version, secure);
		}

	}
}
