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
package com.threewks.thundr.module;

import org.apache.commons.lang3.StringUtils;

import com.threewks.thundr.injection.BaseModule;
import com.threewks.thundr.injection.Module;
import com.threewks.thundr.injection.UpdatableInjectionContext;

public class ModulesModule extends BaseModule {
	public static final String ApplicationClassProperty = "applicationModule";
	private static final String ApplicationModule = "ApplicationModule";

	public ModulesModule() {
	}

	@Override
	public void initialise(UpdatableInjectionContext injectionContext) {
		Modules modules = injectionContext.get(Modules.class);
		Class<? extends Module> applicationModule = loadApplicationModule(injectionContext);
		modules.addModule(applicationModule);
	}

	@SuppressWarnings("unchecked")
	protected Class<? extends Module> loadApplicationModule(UpdatableInjectionContext injectionContext) {
		String className = StringUtils.defaultString(injectionContext.get(String.class, ApplicationClassProperty), ApplicationModule);
		Class<?> type = null;
		try {
			type = Class.forName(className);
		} catch (Exception e) {
			throw new ModuleLoadingException(
					e,
					"Failed to load the application module. You must provide an implementation of '%s', either as '%s' in the default package, or you can specify the full class name in the configuration property '%s'. In this case we failed trying to load '%s' - error: %s",
					Module.class.getName(), ApplicationModule, ApplicationClassProperty, className, e.getMessage());
		}
		if (Module.class.isAssignableFrom(type)) {
			return (Class<? extends Module>) type;
		} else {
			throw new ModuleLoadingException("Failed to load the application module. '%s' does not implement '%s'", type.getName(), Module.class.getName());
		}
	}
}
