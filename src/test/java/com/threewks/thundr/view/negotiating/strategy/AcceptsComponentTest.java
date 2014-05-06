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
package com.threewks.thundr.view.negotiating.strategy;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.atomicleopard.expressive.EList;
import com.atomicleopard.expressive.Expressive;
import com.threewks.thundr.view.negotiating.strategy.AcceptsHeaderNegotiationStrategy.AcceptsComponent;

public class AcceptsComponentTest {

	@Test
	public void shouldBreakAcceptHeaderComponentDownIntoElements() {
		AcceptsComponent acceptsComponent = new AcceptsComponent("application/javascript");
		assertThat(acceptsComponent.getAccept(), is("application/javascript"));
		assertThat(acceptsComponent.getQuality(), is(1f));
		assertThat(acceptsComponent.getComponents(), is(1));
	}

	@Test
	public void shouldBreakAcceptHeaderComponentDownIntoElementsIgnoringQualityAsInteger() {
		AcceptsComponent acceptsComponent = new AcceptsComponent("application/javascript;q=1");
		assertThat(acceptsComponent.getAccept(), is("application/javascript"));
		assertThat(acceptsComponent.getQuality(), is(1f));
		assertThat(acceptsComponent.getComponents(), is(1));
	}

	@Test
	public void shouldBreakAcceptHeaderComponentDownIntoElementsIgnoringWhitespace() {
		AcceptsComponent acceptsComponent = new AcceptsComponent("  application/javascript;var1;q=1 ");
		assertThat(acceptsComponent.getAccept(), is("application/javascript;var1"));
		assertThat(acceptsComponent.getQuality(), is(1f));
		assertThat(acceptsComponent.getComponents(), is(2));
	}
	
	@Test
	public void shouldBreakAcceptHeaderComponentDownIntoElementsWithMultipleComponents() {
		AcceptsComponent acceptsComponent = new AcceptsComponent("application/javascript;v1");
		assertThat(acceptsComponent.getAccept(), is("application/javascript;v1"));
		assertThat(acceptsComponent.getQuality(), is(1f));
		assertThat(acceptsComponent.getComponents(), is(2));
	}

	@Test
	public void shouldBreakAcceptHeaderComponentDownIntoElementsIgnoringQualityAsFloat() {
		AcceptsComponent acceptsComponent = new AcceptsComponent("application/javascript;q=0.2");
		assertThat(acceptsComponent.getAccept(), is("application/javascript"));
		assertThat(acceptsComponent.getQuality(), is(0.2f));
		assertThat(acceptsComponent.getComponents(), is(1));
	}

	@Test
	public void shouldBreakAcceptHeaderComponentDownIntoElementsIgnoringQualityAsFloatWithoutLeadingNumber() {
		AcceptsComponent acceptsComponent = new AcceptsComponent("application/javascript;q=.3");
		assertThat(acceptsComponent.getAccept(), is("application/javascript"));
		assertThat(acceptsComponent.getQuality(), is(0.3f));
		assertThat(acceptsComponent.getComponents(), is(1));
	}

	@Test
	public void shouldOrderWithHigherQualityFirst() {
		AcceptsComponent first = new AcceptsComponent("first;q=0.7");
		AcceptsComponent second = new AcceptsComponent("second;q=0.5");
		AcceptsComponent third = new AcceptsComponent("third;q=0.3");

		EList<AcceptsComponent> sorted = Expressive.list(third, second, first).sort(AcceptsComponent.Comparator);
		assertThat(sorted.get(0), is(first));
		assertThat(sorted.get(1), is(second));
		assertThat(sorted.get(2), is(third));
	}

	@Test
	public void shouldOrderWithMoreSpecificComponentsFirstRelyingOnOriginalOrderWhenSameSpecifity() {
		AcceptsComponent first = new AcceptsComponent("type;1param");
		AcceptsComponent second = new AcceptsComponent("type;2param");
		AcceptsComponent third = new AcceptsComponent("type");

		EList<AcceptsComponent> sorted = Expressive.list(third, first, second).sort(AcceptsComponent.Comparator);
		assertThat(sorted.get(0), is(first));
		assertThat(sorted.get(1), is(second));
		assertThat(sorted.get(2), is(third));
	}

	@Test
	public void shouldOrderWhenComponentSpecifityAndQualityAreMixed() {
		AcceptsComponent first = new AcceptsComponent("type;1param");
		AcceptsComponent second = new AcceptsComponent("type;2param;q=0.9");
		AcceptsComponent third = new AcceptsComponent("type;q=0.9");

		EList<AcceptsComponent> sorted = Expressive.list(third, second, first).sort(AcceptsComponent.Comparator);
		assertThat(sorted.get(0), is(first));
		assertThat(sorted.get(1), is(second));
		assertThat(sorted.get(2), is(third));
	}
	
	@Test
	public void shouldHaveToString() {
		assertThat(new AcceptsComponent("application/javascript;var1;q=1.0").toString(), is("application/javascript;var1"));
		
	}
}
