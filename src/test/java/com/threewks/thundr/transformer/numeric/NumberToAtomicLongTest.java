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
package com.threewks.thundr.transformer.numeric;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;

import org.junit.Test;

public class NumberToAtomicLongTest {
	private NumberToAtomicLong transformer = new NumberToAtomicLong();

	@Test
	public void shouldTransform() {
		assertThat(transformer.from(null), is(nullValue()));
		assertThat(transformer.from(BigDecimal.ZERO).longValue(), is(0l));
		assertThat(transformer.from(BigDecimal.ONE).longValue(), is(1l));
		assertThat(transformer.from(BigDecimal.TEN).longValue(), is(10l));
		assertThat(transformer.from(new BigDecimal(1234)).longValue(), is(1234l));
		assertThat(transformer.from(new BigDecimal("1234.0000001")).longValue(), is(1234l));
		assertThat(transformer.from(new BigDecimal("0.000000000000000000000000000000000012")).longValue(), is(0l));
		assertThat(transformer.from(new BigDecimal("-0.000000000000000000000000000000000012")).longValue(), is(0l));
		assertThat(transformer.from(0).longValue(), is(0l));
		assertThat(transformer.from(1).longValue(), is(1l));
		assertThat(transformer.from(10).longValue(), is(10l));
		assertThat(transformer.from(1234.0).longValue(), is(1234l));
		assertThat(transformer.from(1234.0f).longValue(), is(1234l));
		assertThat(transformer.from(10).longValue(), is(10l));
		assertThat(transformer.from((int) 11).longValue(), is(11l));
	}
}
