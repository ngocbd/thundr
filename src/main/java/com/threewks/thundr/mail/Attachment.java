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

/**
 * An email attachment.
 */
public interface Attachment {

	/**
	 * Get the relative file name of the attachment. This should not include any
	 * path information.
	 *
	 * @return the file name
	 */
	String getName();

	/**
	 * Get the content type of the attachment.
	 *
	 * @return the content type
	 */
	String getContentType();

	/**
	 * Get the content ID for inline attachments.
	 *
	 * @return the content ID;
	 */
	String getContentId();

	/**
	 * Get the attachment data.
	 *
	 * @return the attachment data
	 */
	byte[] getData();
}
