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
package com.threewks.thundr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.atomicleopard.expressive.Cast;
import com.threewks.thundr.configuration.ConfigurationModule;
import com.threewks.thundr.injection.InjectionContextImpl;
import com.threewks.thundr.injection.Module;
import com.threewks.thundr.injection.UpdatableInjectionContext;
import com.threewks.thundr.logger.Logger;
import com.threewks.thundr.module.Modules;
import com.threewks.thundr.module.ModulesModule;
import com.threewks.thundr.request.MutableRequestContainer;
import com.threewks.thundr.request.Request;
import com.threewks.thundr.request.RequestModule;
import com.threewks.thundr.request.Response;
import com.threewks.thundr.route.HttpMethod;
import com.threewks.thundr.route.Route;
import com.threewks.thundr.route.RouteResolverException;
import com.threewks.thundr.route.Router;
import com.threewks.thundr.route.RouterModule;
import com.threewks.thundr.transformer.TransformerModule;
import com.threewks.thundr.view.ViewResolverNotFoundException;
import com.threewks.thundr.view.ViewResolverRegistry;

/**
 *
 */
public class Thundr {
	@SuppressWarnings("unchecked")
	private static final Class<? extends Module>[] None = new Class[0];

	protected UpdatableInjectionContext injectionContext;
	protected Modules modules;
	protected boolean started = false;
	protected boolean stopped = false;
	protected List<Class<? extends Module>> coreModules = new ArrayList<>();

	public Thundr() {
		this(None);
	}

	@SafeVarargs
	public Thundr(Class<? extends Module>... modules) {
		this.injectionContext = new InjectionContextImpl();
		this.modules = new Modules();
		this.coreModules.addAll(Arrays.asList(modules));
	}

	public UpdatableInjectionContext getInjectionContext() {
		return injectionContext;
	}

	public Modules getModules() {
		return modules;
	}

	public boolean isStopped() {
		return stopped;
	}

	public boolean isStarted() {
		return started;
	}

	public void start() {
		long start = System.currentTimeMillis();
		initModules(injectionContext, modules);
		debugRoutes(injectionContext);
		this.started = true;
		Logger.info("Started up in %dms", System.currentTimeMillis() - start);
	}

	public void stop() {
		modules.runStopLifecycle(injectionContext);
		stopped = true;
	}

	private void debugRoutes(UpdatableInjectionContext injectionContext) {
		Router router = injectionContext.get(Router.class);
		if (router == null || router.isEmpty()) {
			Logger.warn("No routes are configured for this application.");
		}
		if (Logger.willDebug()) {
			Logger.debug("Loaded routes: \n%s", router.listRoutes());
		}
	}

	protected Modules initModules(UpdatableInjectionContext injectionContext, Modules modules) {
		injectionContext.inject(modules).as(Modules.class);

		for (Class<? extends Module> module : getBaseModules()) {
			modules.addModule(module);
		}
		modules.runStartupLifecycle(injectionContext);
		return modules;
	}

	protected List<Class<? extends Module>> getBaseModules() {
		List<Class<? extends Module>> baseModules = new ArrayList<Class<? extends Module>>();
		baseModules.add(ConfigurationModule.class);
		baseModules.add(ModulesModule.class);
		baseModules.add(TransformerModule.class);
		baseModules.add(RequestModule.class);
		baseModules.add(RouterModule.class);
		for (Class<? extends Module> module : coreModules) {
			baseModules.add(module);
		}
		return baseModules;
	}

	public Route findRoute(HttpMethod method, String routePath) {
		Router router = injectionContext.get(Router.class);
		return router.findMatchingRoute(method, routePath);
	}

	/**
	 * Resolves the given request into the given response.
	 * 
	 * @param req
	 * @param resp
	 */
	public void resolve(Request req, Response resp) {
		MutableRequestContainer requestContainer = injectionContext.get(MutableRequestContainer.class);
		ViewResolverRegistry viewResolverRegistry = injectionContext.get(ViewResolverRegistry.class);
		Router router = injectionContext.get(Router.class);

		try {
			requestContainer.set(req, resp);
			Object view = router.resolve(req, resp);
			if (view != null) {
				viewResolverRegistry.resolve(req, resp, view);
			}
			resp.finaliseResponse();
		} catch (RuntimeException e) {
			if (Cast.is(e, RouteResolverException.class)) {
				// unwrap RouteResolverException if it is one
				e = (RuntimeException) Cast.as(e, RouteResolverException.class).getCause();
			}
			if (Cast.is(e, ViewResolverNotFoundException.class)) {
				// if there was an error finding a view resolver, propagate this
				throw (ViewResolverNotFoundException) e;
			}
			if (resp.isUncommitted()) {
				try {
					viewResolverRegistry.resolve(req, resp, e);
					resp.finaliseResponse();
				} catch (ViewResolverNotFoundException exceptionViewNotFound) {
					throw e;
				}
			}
		} finally {
			requestContainer.clear();
		}
	}
}