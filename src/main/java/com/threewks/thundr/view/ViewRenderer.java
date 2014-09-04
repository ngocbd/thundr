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

/**
 * A {@link ViewRenderer} resolves the given view and writes it to some output.
 * Typically this would be a servlet response (@see {@link ServletViewRenderer}) or
 * into memory (@see {@link BasicViewRenderer}).
 * 
 * All {@link ViewRenderer} implementations should be considered stateful and should not
 * be reused or shared across threads.
 */
public interface ViewRenderer {
	public void render(Object view);
}
