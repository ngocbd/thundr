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
package com.threewks.thundr.aop;

import java.lang.annotation.Annotation;

public interface AdviceRegistry {
	public <A extends Annotation> void add(Class<A> annotation, Advice<A, ?> interceptor);

	public <A extends Annotation> void remove(Class<A> annotation);

	public boolean contains(Class<? extends Annotation> annotation);

	public <A extends Annotation> boolean contains(Class<A> annotation, Class<? extends Advice<? extends A, ?>> interceptor);

	/**
	 * Given an instance, will wrap it in a proxy which will execute pointcuts when annotated methods are called, if any pointcuts exist.
	 * If not, the original instance is returned
	 * 
	 * @param instance
	 * @return
	 */
	public <T> T proxyIfNeeded(T instance);

}