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
package com.threewks.thundr.mail;

import org.apache.commons.lang3.StringUtils;

import com.threewks.thundr.view.BaseView;
import com.threewks.thundr.view.file.Disposition;
import com.threewks.thundr.view.file.FileView;

/**
 * An email attachment.
 */
public class Attachment {

	private final String name;
	private final BaseView view;
	private final Disposition disposition;

	/**
	 * Create an Attachment.
	 *
	 * @param view the attachment view
	 */
	public Attachment(FileView view) {
		this.name = view.getFileName();
		this.view = view;
		this.disposition = view.getDisposition();
	}

	/**
	 * Create an Attachment.
	 *
	 * @param name the file name of the attachment or content id if disposition is inline
	 * @param view the attachment view
	 * @param disposition the attachment disposition (i.e. inline or not)
	 */
	public Attachment(String name, BaseView view, Disposition disposition) {
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
	 * Get the content type of the attachment.
	 *
	 * @return the content type
	 */
	public String contentType() {
		return view.getContentType();
	}

	/**
	 * Get the attachment view.
	 *
	 * @return the attachment data
	 */
	public BaseView view() {
		return view;
	}

	/**
	 * Get the content ID for inline attachments.
	 *
	 * @return the content ID;
	 */
	public String contentId() {
		StringBuilder builder = new StringBuilder();

		String trimmed = StringUtils.trim(name);
		if (!trimmed.startsWith("<")) builder.append("<");
		builder.append(trimmed);
		if (!trimmed.endsWith(">")) builder.append(">");

		return builder.toString();
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
}
