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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.threewks.thundr.http.ContentType;

public class AttachmentBuilder {

	private String name;
	private String contentType;
	private String contentId;
	private Object data;

	public AttachmentBuilder() {}

	AttachmentBuilder(String name, String contentType, String contentId, Object data) {
		this.name = name;
		this.contentType = contentType;
		this.contentId = contentId;
		this.data = data;
	}

	public AttachmentBuilder name(String name) {
		return new AttachmentBuilder(name, contentType, contentId, data);
	}

	public AttachmentBuilder contentType(ContentType contentType) {
		return new AttachmentBuilder(name, contentType.value(), contentId, data);
	}

	public AttachmentBuilder contentType(String contentType) {
		return new AttachmentBuilder(name, contentType, contentId, data);
	}

	public AttachmentBuilder contentId(String contentId) {
		return new AttachmentBuilder(name, contentType, contentId, data);
	}

	public AttachmentBuilder data(InputStream stream) {
		return new AttachmentBuilder(name, contentType, contentId, stream);
	}

	public AttachmentBuilder data(byte[] data) {
		return new AttachmentBuilder(name, contentType, contentId, data);
	}

	public AttachmentBuilder data(File file) {
		return new AttachmentBuilder(name, contentType, contentId, file);
	}

	public Attachment build() throws MailException {
		if (name == null) throw new MailException("Attachment must include a filename.");
		if (contentType == null) throw new MailException("Attachment must include a content type.");
		if (data == null) throw new MailException("Attachment must include data.");

			/*
			 * This is probably less than ideal but it's nicer to have all possible exceptions
			 * only occur from build rather than during the construction process.
			 */
		byte[] attachmentData = null;
		if (InputStream.class.isAssignableFrom(data.getClass())) {
			try {
				attachmentData = toByteArray((InputStream) data);
			} catch (IOException e) {
				throw new MailException("Failed to create byte[] from InputStream.");
			}
		} else if (File.class.isAssignableFrom(data.getClass())) {
			try {
				attachmentData = toByteArray(new FileInputStream((File) data));
			} catch (IOException e) {
				throw new MailException(e, "Failed to create byte[] from File.");
			}
		} else if (byte[].class.isAssignableFrom(data.getClass())) {
			attachmentData = (byte[]) data;
		}

		return new AttachmentImpl(name, contentType, contentId, attachmentData);
	}

	private byte[] toByteArray(InputStream input) throws IOException {
		int n;
		byte[] buffer = new byte[1024 * 4];
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
		}
		return output.toByteArray();
	}

	private static class AttachmentImpl implements Attachment {
		private final String name;
		private final String contentType;
		private final String contentId;
		private final byte[] data;

		private AttachmentImpl(String name, String contentType, String contentId, byte[] data) {
			this.name = name;
			this.contentType = contentType;
			this.contentId = contentId;
			this.data = data;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getContentType() {
			return contentType;
		}

		@Override
		public String getContentId() {
			return contentId;
		}

		@Override
		public byte[] getData() {
			return data;
		}
	}
}
