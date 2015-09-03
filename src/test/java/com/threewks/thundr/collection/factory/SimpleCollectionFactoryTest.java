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
package com.threewks.thundr.collection.factory;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.threewks.thundr.exception.BaseException;

@SuppressWarnings("rawtypes")
public class SimpleCollectionFactoryTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void shouldCreateCollectionOfSpecifiedType() {
		SimpleCollectionFactory<List> factory = new SimpleCollectionFactory<List>(List.class, ArrayList.class);
		List list = factory.create();
		assertThat(list, is(notNullValue()));
		assertThat(list instanceof ArrayList, is(true));
	}

	@Test
	public void shouldHandleAllTheseTypes() {
		assertThat(new SimpleCollectionFactory<List>(List.class, ArrayList.class).create() instanceof ArrayList, is(true));
		assertThat(new SimpleCollectionFactory<ArrayList>(ArrayList.class, ArrayList.class).create() instanceof ArrayList, is(true));
		assertThat(new SimpleCollectionFactory<List>(List.class, LinkedList.class).create() instanceof LinkedList, is(true));
		assertThat(new SimpleCollectionFactory<LinkedList>(LinkedList.class, LinkedList.class).create() instanceof LinkedList, is(true));
		assertThat(new SimpleCollectionFactory<List>(List.class, Vector.class).create() instanceof Vector, is(true));
		assertThat(new SimpleCollectionFactory<Vector>(Vector.class, Vector.class).create() instanceof Vector, is(true));

		assertThat(new SimpleCollectionFactory<Set>(Set.class, HashSet.class).create() instanceof HashSet, is(true));
		assertThat(new SimpleCollectionFactory<HashSet>(HashSet.class, HashSet.class).create() instanceof HashSet, is(true));
		assertThat(new SimpleCollectionFactory<Set>(Set.class, LinkedHashSet.class).create() instanceof LinkedHashSet, is(true));
		assertThat(new SimpleCollectionFactory<LinkedHashSet>(LinkedHashSet.class, LinkedHashSet.class).create() instanceof LinkedHashSet, is(true));
		assertThat(new SimpleCollectionFactory<HashSet>(HashSet.class, LinkedHashSet.class).create() instanceof LinkedHashSet, is(true));
		assertThat(new SimpleCollectionFactory<SortedSet>(SortedSet.class, TreeSet.class).create() instanceof TreeSet, is(true));

		assertThat(new SimpleCollectionFactory<Collection>(Collection.class, ArrayList.class).create() instanceof ArrayList, is(true));
		assertThat(new SimpleCollectionFactory<Collection>(Collection.class, LinkedList.class).create() instanceof LinkedList, is(true));
		assertThat(new SimpleCollectionFactory<Collection>(Collection.class, Vector.class).create() instanceof Vector, is(true));
		assertThat(new SimpleCollectionFactory<Collection>(Collection.class, HashSet.class).create() instanceof HashSet, is(true));
		assertThat(new SimpleCollectionFactory<Collection>(Collection.class, LinkedHashSet.class).create() instanceof LinkedHashSet, is(true));
		assertThat(new SimpleCollectionFactory<Collection>(Collection.class, TreeSet.class).create() instanceof TreeSet, is(true));
	}

	@Test
	public void shouldThrowBaseExceptionWhenCollectionCannotBeCreated() {
		thrown.expect(BaseException.class);
		thrown.expectMessage("Failed to instantiate a collection of type NoDefaultCtorCollection: ");
		SimpleCollectionFactory<List> factory = new SimpleCollectionFactory<List>(List.class, NoDefaultCtorCollection.class);
		factory.create();

	}

	@SuppressWarnings("unused")
	private static class NoDefaultCtorCollection<T> extends ArrayList<T> {
		private static final long serialVersionUID = 0l;

		public NoDefaultCtorCollection(String requiredArg) {

		}
	}

}
