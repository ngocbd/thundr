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
package com.threewks.thundr.module;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.atomicleopard.expressive.Cast;
import com.atomicleopard.expressive.EList;
import com.atomicleopard.expressive.ETransformer;
import com.atomicleopard.expressive.Expressive;
import com.atomicleopard.expressive.transform.CollectionTransformer;
import com.threewks.thundr.injection.InjectionContext;
import com.threewks.thundr.injection.Module;
import com.threewks.thundr.injection.UpdatableInjectionContext;
import com.threewks.thundr.logger.Logger;

public class Modules {
	private Map<Class<? extends Module>, Collection<Class<? extends Module>>> moduleDependencies = new LinkedHashMap<Class<? extends Module>, Collection<Class<? extends Module>>>();
	private Map<Class<? extends Module>, Module> instances = new LinkedHashMap<Class<? extends Module>, Module>();
	private Map<Module, ModuleStatus> status = new HashMap<Module, ModuleStatus>();
	private List<Module> orderedModules = null;

	public Modules() {
	}

	public void addModule(Class<? extends Module> module) {
		if (!hasModule(module)) {
			Module instance = loadModule(module);
			moduleDependencies.put(module, null);
			instances.put(module, instance);
			status.put(instance, ModuleStatus.Added);
			Logger.debug("Added module %s", Transformers.toModuleName.from(module));
		}
	}

	public <T extends Module> T getModule(Class<T> moduleClass) {
		return Cast.as(instances.get(moduleClass), moduleClass);
	}

	public List<? extends Module> getModules(Collection<Class<? extends Module>> moduleClasses) {
		List<Module> result = new ArrayList<Module>();
		for (Class<? extends Module> moduleClass : moduleClasses) {
			Module instance = instances.get(moduleClass);
			if (instance != null) {
				result.add(instance);
			}
		}
		return result;
	}

	public boolean hasModule(Class<? extends Module> moduleClass) {
		return instances.containsKey(moduleClass);
	}

	public List<Module> listModules() {
		return new ArrayList<Module>(instances.values());
	}

	public void runStartupLifecycle(UpdatableInjectionContext injectionContext) {
		Logger.debug("Loading modules...");
		List<Module> startupOrder = new ArrayList<Module>();
		while (!allModulesStarted()) {
			if (status.values().contains(ModuleStatus.Added)) {
				resolveDependencies();
				orderedModules = determineDependencyOrder();
			} else {
				boolean allInitialised = initialiseNext(injectionContext);
				boolean allConfigured = false;
				if (allInitialised) {
					allConfigured = configureNext(injectionContext);
				}
				if (allConfigured) {
					Module started = startNext(injectionContext);
					if (started != null) {
						startupOrder.add(started);
					}
				}
			}
		}
		Logger.info("Modules loaded");
		if (Logger.willDebug()) {
			StringBuilder sb = new StringBuilder();
			for (Module injectionConfiguration : startupOrder) {
				sb.append("\n\t");
				sb.append(injectionConfiguration.getClass().getSimpleName());
			}
			Logger.debug("Modules started in this order:%s", sb.toString());

		}
	}

	/**
	 * @param injectionContext
	 * @return true if all modules are initialised
	 */
	private boolean initialiseNext(UpdatableInjectionContext injectionContext) {
		Module initialise = getFirstModuleWithStatus(ModuleStatus.DependenciesResolved);
		if (initialise != null) {
			initialise.initialise(injectionContext);
			status.put(initialise, ModuleStatus.Initialised);
		}
		return initialise == null;
	}

	/**
	 * @param injectionContext
	 * @return true if all modules are already initialised
	 */
	private boolean configureNext(UpdatableInjectionContext injectionContext) {
		Module configure = getFirstModuleWithStatus(ModuleStatus.Initialised);
		if (configure != null) {
			configure.configure(injectionContext);
			status.put(configure, ModuleStatus.Configured);
		}
		return configure == null;
	}

	private Module startNext(UpdatableInjectionContext injectionContext) {
		Module start = getFirstModuleWithStatus(ModuleStatus.Configured);
		if (start != null) {
			start.start(injectionContext);
			status.put(start, ModuleStatus.Started);
		}
		return start;
	}

	private Module getFirstModuleWithStatus(ModuleStatus status) {
		Module result = null;
		for (Module injectionConfiguration : orderedModules) {
			if (status.equals(this.status.get(injectionConfiguration))) {
				return injectionConfiguration;
			}
		}
		return result;
	}

	private boolean allModulesStarted() {
		Collection<ModuleStatus> values = status.values();
		return !values.contains(ModuleStatus.Added) && !values.contains(ModuleStatus.DependenciesResolved) && !values.contains(ModuleStatus.Initialised) && !values.contains(ModuleStatus.Configured);
	}

	public void runStopLifecycle(InjectionContext injectionContext) {
		List<Module> reverseOrder = new LinkedList<Module>(orderedModules);
		Collections.reverse(reverseOrder);
		for (Module injectionConfiguration : reverseOrder) {
			injectionConfiguration.stop(injectionContext);
			status.put(injectionConfiguration, ModuleStatus.Stopped);
		}
	}

	/**
	 * Determines the dependency order of all modules.
	 * 
	 * @return
	 */
	protected List<Module> determineDependencyOrder() {
		List<Module> orderedModules = new ArrayList<Module>();

		while (!orderedModules.containsAll(instances.values())) {
			boolean anyAdded = false;
			for (Map.Entry<Class<? extends Module>, Module> entry : instances.entrySet()) {
				Module instance = entry.getValue();

				if (!orderedModules.contains(instance)) {
					Class<? extends Module> configurationClass = entry.getKey();

					Collection<Class<? extends Module>> dependencies = moduleDependencies.get(configurationClass);
					List<? extends Module> injectionConfigurations = getModules(dependencies);
					if (orderedModules.containsAll(injectionConfigurations)) {
						orderedModules.add(instance);
						anyAdded = true;
					}
				}
			}
			if (!anyAdded) {
				List<Module> unloaded = Expressive.list(instances.values()).removeItems(orderedModules);
				EList<String> moduleNames = Transformers.toModuleNamesFromInstance.from(unloaded);
				throw new ModuleLoadingException(
						"Unable to load modules - there are unloaded modules whose dependencies cannot be satisfied. This probably indicates a cyclical dependency. The following modules have not been loaded: %s",
						StringUtils.join(moduleNames, " "));
			}
		}
		return orderedModules;
	}

	/**
	 * Causes the dependent modules for any modules already added to be added as well.
	 */
	public void resolveDependencies() {
		while (hasMoreDependenciesToResolve()) {
			for (Module injectionConfiguration : getModulesWithUnresolvedDependencies()) {

				Class<? extends Module> moduleClass = injectionConfiguration.getClass();

				DependencyRegistry dependencyRegistry = new DependencyRegistry();
				injectionConfiguration.requires(dependencyRegistry);
				Collection<Class<? extends Module>> dependencies = dependencyRegistry.getDependencies();
				String moduleName = Transformers.toModuleName.from(moduleClass);
				for (Class<? extends Module> dependencyClass : dependencies) {
					addModule(dependencyClass);
					Logger.debug("Module %s depends on %s", moduleName, Transformers.toModuleName.from(dependencyClass));
				}

				moduleDependencies.put(moduleClass, dependencies);
				status.put(injectionConfiguration, ModuleStatus.DependenciesResolved);
			}
		}
	}

	private Collection<Module> getModulesWithUnresolvedDependencies() {
		Collection<Module> result = new ArrayList<Module>();
		for (Map.Entry<Module, ModuleStatus> loaded : status.entrySet()) {
			if (ModuleStatus.Added.equals(loaded.getValue())) {
				result.add(loaded.getKey());
			}
		}
		return result;
	}

	private boolean hasMoreDependenciesToResolve() {
		return status.values().contains(ModuleStatus.Added);
	}

	protected static Module loadModule(Class<? extends Module> moduleClass) {
		try {
			Object newInstance = moduleClass.newInstance();
			Module configuration = Cast.as(newInstance, Module.class);
			if (configuration == null) {
				throw new ModuleLoadingException("Failed to load module '%s' - the configuration class %s does not implement '%s'", moduleClass.getName(), Module.class.getName());
			}
			return configuration;
		} catch (InstantiationException e) {
			throw new ModuleLoadingException(e, moduleClass.getPackage().getName(), "failed to instantiate configuration class %s: %s", moduleClass.getName(), e.getMessage());
		} catch (IllegalAccessException e) {
			throw new ModuleLoadingException(e, moduleClass.getPackage().getName(), "cannot instantiate configuration class %s: %s", moduleClass.getName(), e.getMessage());
		}
	}

	public static class Transformers {
		public static final ETransformer<Class<?>, String> toModuleName = new ETransformer<Class<?>, String>() {
			@Override
			public String from(Class<?> from) {
				return from.getName();
			}
		};
		public static final CollectionTransformer<Class<?>, String> toModuleNames = Expressive.Transformers.transformAllUsing(toModuleName);
		public static final ETransformer<Module, String> toModuleNameFromInstance = new ETransformer<Module, String>() {
			@Override
			public String from(Module from) {
				return from.getClass().getName();
			}
		};
		public static final CollectionTransformer<Module, String> toModuleNamesFromInstance = Expressive.Transformers.transformAllUsing(toModuleNameFromInstance);
	}

	private enum ModuleStatus {
		Added,
		DependenciesResolved,
		Initialised,
		Configured,
		Started,
		Stopped;
	}
}
