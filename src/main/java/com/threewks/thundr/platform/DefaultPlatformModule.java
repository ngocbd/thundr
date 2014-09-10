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
package com.threewks.thundr.platform;

import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import com.threewks.thundr.configuration.Environment;
import com.threewks.thundr.injection.InjectionContext;
import com.threewks.thundr.injection.Module;
import com.threewks.thundr.injection.UpdatableInjectionContext;
import com.threewks.thundr.logger.Logger;
import com.threewks.thundr.module.DependencyRegistry;

/**
 * Sets the thundr {@link Environment} from the system property 'thundrEnvironment' (i.e. -DthundrEnvironment=)
 * or an environment variable of the same name if no system property is present.
 * If neither is present, {@link Environment#DEV} is assumed.
 */
public class DefaultPlatformModule implements Module {
	private Properties systemProperties = System.getProperties();
	private Map<String, String> environmentVariables = System.getenv();

	@Override
	public void requires(DependencyRegistry dependencyRegistry) {
	}

	@Override
	public void initialise(UpdatableInjectionContext injectionContext) {
		String environment = systemProperties.getProperty("thundrEnvironment");
		if (StringUtils.isBlank(environment)) {
			environment = environmentVariables.get("thundrEnvironment");
		}
		if (StringUtils.isBlank(environment)) {
			environment = Environment.DEV;
		}

		Environment.set(environment);
		Logger.info("Running as environment %s", environment);
		injectionContext.inject(environment).named("environment").as(String.class);
	}

	@Override
	public void configure(UpdatableInjectionContext injectionContext) {
	}

	@Override
	public void start(UpdatableInjectionContext injectionContext) {
	}

	@Override
	public void stop(InjectionContext injectionContext) {
	}

}
