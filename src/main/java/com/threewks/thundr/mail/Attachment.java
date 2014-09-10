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
package com.threewks.thundr.mail;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.threewks.thundr.view.file.Disposition;

import jodd.util.StringUtil;

/**
 * An email attachment.
 */
public class Attachment {

	private final String name;
	private final Object view;
	private final Disposition disposition;

	/**
	 * Create an Attachment.
	 * 
	 * @param name the file name of the attachment or content id if disposition is inline
	 * @param view the attachment view
	 * @param disposition the attachment disposition (i.e. inline or not)
	 */
	public Attachment(String name, Object view, Disposition disposition) {
		this.name = name;
		this.view = view;
		this.disposition = disposition;
	}

	/**
	 * Get the relative file name of the attachment. This should not include any
	 * path information.
	 * 
	 * @return the file name
	 */
	public String name() {
		return name;
	}

	/**
	 * Get the attachment view.
	 * 
	 * @return the attachment data
	 */
	public Object view() {
		return view;
	}

	/**
	 * Get the content ID for inline attachments. This is simply `name`
	 * surrounded with angle brackets (i.e. <name>) as it required for content IDs.
	 * 
	 * @return the content ID;
	 */
	public String contentId() {
		return StringUtil.surround(name.trim(), "<", ">");
	}

	/**
	 * Get the attachment content disposition.
	 * 
	 * @return the disposition
	 */
	public Disposition disposition() {
		return disposition;
	}

	/**
	 * Return true if the attachment is inline.
	 * 
	 * @return true if attachment is inline, otherwise false
	 */
	public boolean isInline() {
		return disposition == Disposition.Inline;
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj, false);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, false);
	}
}
