package com.threewks.thundr;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atomicleopard.expressive.Cast;
import com.threewks.thundr.action.ActionException;
import com.threewks.thundr.injection.DefaultInjectionConfiguration;
import com.threewks.thundr.injection.InjectionConfiguration;
import com.threewks.thundr.injection.InjectionContextImpl;
import com.threewks.thundr.injection.UpdatableInjectionContext;
import com.threewks.thundr.logger.Logger;
import com.threewks.thundr.profiler.Profilable;
import com.threewks.thundr.profiler.Profiler;
import com.threewks.thundr.route.RouteType;
import com.threewks.thundr.route.Routes;
import com.threewks.thundr.view.ViewResolver;
import com.threewks.thundr.view.ViewResolverRegistry;

public class WebFrameworkServlet extends HttpServlet {
	private static final long serialVersionUID = -7179293239117252585L;
	private UpdatableInjectionContext injectionContext;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		try {
			ServletContext servletContext = config.getServletContext();
			injectionContext = new InjectionContextImpl();
			injectionContext.inject(servletContext).as(ServletContext.class);
			InjectionConfiguration injectionConfiguration = getInjectionConfigInstance(servletContext);
			injectionConfiguration.configure(injectionContext);
		} catch (RuntimeException e) {
			throw new ServletException("Failed to initialse thundr: " + e.getMessage(), e);
		}
	}

	protected InjectionConfiguration getInjectionConfigInstance(ServletContext servletContext) {
		return new DefaultInjectionConfiguration();
	}

	protected void applyRoute(final RouteType routeType, final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		final ViewResolverRegistry viewResolverRegistry = injectionContext.get(ViewResolverRegistry.class);
		String requestPath = req.getRequestURI();
		Profiler profiler = injectionContext.get(Profiler.class);
		profiler.beginProfileSession(routeType.name() + " " + requestPath);
		req.setAttribute("com.threewks.thundr.profiler.Profiler", profiler);
		try {
			Logger.debug("Invoking path %s", requestPath);
			Routes routes = injectionContext.get(Routes.class);
			final Object viewResult = routes.invoke(requestPath, routeType, req, resp);
			if (viewResult != null) {
				resolveView(req, resp, viewResolverRegistry, viewResult, profiler);
			}
		} catch (Exception e) {
			if (Cast.is(e, ActionException.class)) {
				// unwrap ActionException if it is one
				e = (Exception) Cast.as(e, ActionException.class).getCause();
			}
			if (!resp.isCommitted()) {
				ViewResolver<Exception> viewResolver = viewResolverRegistry.findViewResolver(e);
				if (viewResolver != null) {
					viewResolver.resolve(req, resp, e);
				}
			}
		}
		profiler.endProfileSession();
	}

	private void resolveView(final HttpServletRequest req, final HttpServletResponse resp, final ViewResolverRegistry viewResolverRegistry, final Object viewResult, Profiler profiler) {
		profiler.profile(Profiler.CategoryView, viewResult.toString(), new Profilable<Void>() {
			public Void profile() {
				ViewResolver<Object> viewResolver = viewResolverRegistry.findViewResolver(viewResult);
				viewResolver.resolve(req, resp, viewResult);
				return null;
			}
		});
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		applyRoute(RouteType.GET, req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String method = req.getParameter("_method");
		RouteType routeType = RouteType.POST;
		if ("PUT".equalsIgnoreCase(method)) {
			routeType = RouteType.PUT;
		} else if ("DELETE".equalsIgnoreCase(method)) {
			routeType = RouteType.DELETE;
		}
		applyRoute(routeType, req, resp);
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		applyRoute(RouteType.DELETE, req, resp);
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		applyRoute(RouteType.PUT, req, resp);
	}
}