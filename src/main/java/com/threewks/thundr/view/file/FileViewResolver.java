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
package com.threewks.thundr.view.file;

import java.io.InputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.threewks.thundr.http.Header;
import com.threewks.thundr.logger.Logger;
import com.threewks.thundr.util.Streams;
import com.threewks.thundr.view.BaseView;
import com.threewks.thundr.view.ViewResolutionException;
import com.threewks.thundr.view.ViewResolver;

public class FileViewResolver implements ViewResolver<FileView> {
	@Override
	public void resolve(HttpServletRequest req, HttpServletResponse resp, FileView viewResult) {
		InputStream inputStream = viewResult.getData();
		try {
			ServletOutputStream outputStream = resp.getOutputStream();
			resp.addHeader(Header.ContentDisposition, String.format("%s; filename=%s", viewResult.getDisposition().value(), viewResult.getFileName()));
			BaseView.applyToResponse(viewResult, resp);
			Streams.copy(inputStream, outputStream);
			outputStream.flush();
		} catch (Exception e) {
			throw new ViewResolutionException(e, "Failed to write FileView result: %s", e.getMessage());
		} finally {
			closeInputStream(inputStream);
		}
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}

	protected void closeInputStream(InputStream inputStream) {
		try {
			inputStream.close();
		} catch (Exception e) {
			Logger.warn("Failed to close InputStream while writing file output - %s: %s", e.getClass().getSimpleName(), e.getMessage());
		}
	}
}