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
package com.threewks.thundr.view;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class GlobalModelTest {

	@Test
	public void shouldAllowPutAndRemoveOfItems() {
		GlobalModel globalModel = new GlobalModel();
		assertThat(globalModel.size(), is(0));
		assertThat(globalModel.get("key"), is(nullValue()));
		assertThat(globalModel.containsKey("key"), is(false));
		assertThat(globalModel.containsValue("value"), is(false));

		globalModel.put("key", "value");
		assertThat(globalModel.size(), is(1));
		assertThat(globalModel.containsKey("key"), is(true));
		assertThat(globalModel.get("key"), is((Object) "value"));
		assertThat(globalModel.containsValue("value"), is(true));

		globalModel.put("key", "other");
		assertThat(globalModel.size(), is(1));
		assertThat(globalModel.containsKey("key"), is(true));
		assertThat(globalModel.get("key"), is((Object) "other"));
		assertThat(globalModel.containsValue("value"), is(false));

		globalModel.remove("key");
		assertThat(globalModel.size(), is(0));
		assertThat(globalModel.containsKey("key"), is(false));
		assertThat(globalModel.get("key"), is(nullValue()));
		assertThat(globalModel.containsValue("other"), is(false));
	}
}
