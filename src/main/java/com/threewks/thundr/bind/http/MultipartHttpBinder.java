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
package com.threewks.thundr.bind.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.MultipartStream.MalformedStreamException;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.UploadContext;

import com.threewks.thundr.bind.BindException;
import com.threewks.thundr.bind.Binder;
import com.threewks.thundr.bind.parameter.ParameterBinderRegistry;
import com.threewks.thundr.http.ContentType;
import com.threewks.thundr.http.MultipartFile;
import com.threewks.thundr.introspection.ParameterDescription;
import com.threewks.thundr.logger.Logger;
import com.threewks.thundr.request.Request;
import com.threewks.thundr.request.Response;
import com.threewks.thundr.util.Streams;

public class MultipartHttpBinder implements Binder {

	private List<ContentType> supportedContentTypes = Arrays.asList(ContentType.MultipartFormData);
	private FileUpload upload = new FileUpload();
	private ParameterBinderRegistry parameterBinderRegistry;

	public MultipartHttpBinder(ParameterBinderRegistry parameterBinderRegistry) {
		super();
		this.parameterBinderRegistry = parameterBinderRegistry;
	}

	@Override
	public void bindAll(Map<ParameterDescription, Object> bindings, Request req, Response resp) {
		boolean supported = supportedContentTypes.contains(req.getContentType());
		if (supported && shouldTryToBind(bindings)) {
			Map<String, List<String>> formFields = new HashMap<String, List<String>>();
			Map<String, MultipartFile> fileFields = new HashMap<String, MultipartFile>();
			extractParameters(req, formFields, fileFields);
			parameterBinderRegistry.bind(bindings, formFields, fileFields);
		}
	}

	/**
	 * If all parameters have been bound, we don't need to try to bind. This means we won't consume the stream leaving it in tact to be read in controllers.
	 * 
	 * @param bindings
	 * @return
	 */
	boolean shouldTryToBind(Map<ParameterDescription, Object> bindings) {
		return bindings.values().contains(null);
	}

	void extractParameters(Request req, Map<String, List<String>> formFields, Map<String, MultipartFile> fileFields) {
		try {
			ThundrRequestContext context = new ThundrRequestContext(req);
			FileItemIterator itemIterator = upload.getItemIterator(context);
			while (itemIterator.hasNext()) {
				FileItemStream item = itemIterator.next();
				InputStream stream = item.openStream();

				String fieldName = item.getFieldName();
				if (item.isFormField()) {
					List<String> existing = formFields.get(fieldName);
					if (existing == null) {
						existing = new LinkedList<String>();
						formFields.put(fieldName, existing);
					}
					existing.add(Streams.readString(stream));
				} else {
					MultipartFile file = new MultipartFile(item.getName(), Streams.readBytes(stream), item.getContentType());
					fileFields.put(fieldName, file);
				}
				// stream.close();
			}
		} catch (MalformedStreamException e) {
			Logger.warn("Recieved a malformed multipart stream - data may be missing or unbound, continuing with the request anyway. Error message: %s", e.getMessage());
		} catch (Exception e) {
			throw new BindException(e, "Failed to bind multipart form data: %s", e.getMessage());
		}
	}

	protected static class ThundrRequestContext implements RequestContext, UploadContext {
		private Request req;

		public ThundrRequestContext(Request req) {
			super();
			this.req = req;
		}

		@Override
		public String getCharacterEncoding() {
			return req.getCharacterEncoding();
		}

		@Override
		public String getContentType() {
			return req.getContentTypeString();
		}

		@Override
		public int getContentLength() {
			return (int) req.getContentLength();
		}

		@Override
		public long contentLength() {
			return req.getContentLength();
		}

		@Override
		public InputStream getInputStream() throws IOException {
			return req.getInputStream();
		}
	}
}
