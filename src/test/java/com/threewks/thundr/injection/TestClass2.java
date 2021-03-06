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
package com.threewks.thundr.injection;

public class TestClass2 {
	private String arg1;
	@SuppressWarnings("unused") private String arg2;

	public TestClass2(String arg1) {
		super();
		this.arg1 = arg1;
	}

	public TestClass2(boolean throwsException) {
		super();
		if (throwsException) {
			throw new RuntimeException("expected");
		}
	}

	public String getArg1() {
		return arg1;
	}

	public void setArg2(boolean arg2) {
		if (arg2) {
			throw new RuntimeException("expected");
		}
	}
}
