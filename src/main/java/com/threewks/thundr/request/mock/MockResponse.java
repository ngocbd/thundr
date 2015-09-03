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
package com.threewks.thundr.request.mock;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.threewks.thundr.request.InMemoryResponse;
import com.threewks.thundr.request.Response;
import com.threewks.thundr.transformer.TransformerManager;

public class MockResponse extends InMemoryResponse implements Response {
	protected Object rawResponse;
	protected boolean committed = false;

	public MockResponse() {
		this(TransformerManager.createWithDefaults());
	}

	public MockResponse(TransformerManager transformerManager) {
		super(transformerManager);
		output = new ByteArrayOutputStream() {
			@Override
			public void flush() throws IOException {
				MockResponse.this.committed = true;
			};
		};
	}

	@Override
	public boolean isCommitted() {
		return committed;
	}

	@Override
	protected Object getRawResponse() {
		return rawResponse;
	}

	public MockResponse withRawResponse(Object raw) {
		this.rawResponse = raw;
		return this;
	}

}
