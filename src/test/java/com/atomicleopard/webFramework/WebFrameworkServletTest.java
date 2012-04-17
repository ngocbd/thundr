package com.atomicleopard.webFramework;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.atomicleopard.webFramework.injection.DefaultInjectionConfiguration;
import com.atomicleopard.webFramework.injection.InjectionConfiguration;
import com.atomicleopard.webFramework.injection.InjectionContextImpl;
import com.atomicleopard.webFramework.injection.UpdatableInjectionContext;
import com.atomicleopard.webFramework.route.RouteType;
import com.atomicleopard.webFramework.route.Routes;
import com.atomicleopard.webFramework.test.TestSupport;
import com.atomicleopard.webFramework.test.mock.servlet.MockHttpServletRequest;
import com.atomicleopard.webFramework.test.mock.servlet.MockHttpServletResponse;
import com.atomicleopard.webFramework.test.mock.servlet.MockServletConfig;
import com.atomicleopard.webFramework.test.mock.servlet.MockServletContext;
import com.atomicleopard.webFramework.view.ViewResolver;
import com.atomicleopard.webFramework.view.ViewResolverRegistry;

public class WebFrameworkServletTest {
	private WebFrameworkServlet servlet = new WebFrameworkServlet();
	private UpdatableInjectionContext injectionContext;
	private MockHttpServletResponse resp = new MockHttpServletResponse();
	private Routes routes = mock(Routes.class);
	private ViewResolverRegistry viewResolverRegistry;

	@Before
	public void before() {
		injectionContext = new InjectionContextImpl();
		setInjectionContextIntoServlet(injectionContext);

		when(routes.invoke(anyString(), Mockito.any(RouteType.class), Mockito.any(HttpServletRequest.class), Mockito.any(HttpServletResponse.class))).thenReturn("View Name");
		injectionContext.inject(Routes.class).as(routes);

		viewResolverRegistry = new ViewResolverRegistry();
		injectionContext.inject(ViewResolverRegistry.class).as(viewResolverRegistry);
	}

	@SuppressWarnings("serial")
	@Test
	public void shouldInitializeInjectionContextOnServletInit() throws ServletException {
		ServletContext servletContext = new MockServletContext();
		ServletConfig config = new MockServletConfig(servletContext);
		final InjectionConfiguration injectionConfiguration = mock(InjectionConfiguration.class);
		WebFrameworkServlet servlet = new WebFrameworkServlet() {
			protected InjectionConfiguration getInjectionConfigInstance(ServletContext servletContext) {
				return injectionConfiguration;
			};
		};
		servlet.init(config);
		UpdatableInjectionContext injectionContext = getInjectionContextFromServlet(servlet);
		assertThat(injectionContext, is(notNullValue()));
		assertThat(injectionContext.get(ServletContext.class), is(servletContext));

		verify(injectionConfiguration).configure(injectionContext);
	}

	@Test
	public void shouldUseApplicationDefaultConfiguration() {
		ServletContext servletContext = new MockServletContext();
		assertThat(servlet.getInjectionConfigInstance(servletContext), is(DefaultInjectionConfiguration.class));

	}

	@Test
	public void shouldApplyGetRouteWhenDoGet() throws ServletException, IOException {
		MockHttpServletRequest req = new MockHttpServletRequest();
		servlet = spy(servlet);
		servlet.doGet(req, resp);
		verify(servlet).applyRoute(RouteType.GET, req, resp);
	}

	@Test
	public void shouldApplyPostRouteWhenDoPost() throws ServletException, IOException {
		MockHttpServletRequest req = new MockHttpServletRequest();
		servlet = spy(servlet);
		servlet.doPost(req, resp);
		verify(servlet).applyRoute(RouteType.POST, req, resp);
	}

	@Test
	public void shouldApplyPutRouteWhenDoPostWithPutMethodParameter() throws ServletException, IOException {
		MockHttpServletRequest req = new MockHttpServletRequest();
		req.parameter("_method", "pUT");
		servlet = spy(servlet);
		servlet.doPost(req, resp);
		verify(servlet).applyRoute(RouteType.PUT, req, resp);
	}

	@Test
	public void shouldApplyDeleteRouteWhenDoPostWithDeleteMethodParameter() throws ServletException, IOException {
		MockHttpServletRequest req = new MockHttpServletRequest();
		req.parameter("_method", "dEleTE");
		servlet = spy(servlet);
		servlet.doPost(req, resp);
		verify(servlet).applyRoute(RouteType.DELETE, req, resp);
	}

	@Test
	public void shouldApplyPutRouteWhenDoPut() throws ServletException, IOException {
		MockHttpServletRequest req = new MockHttpServletRequest();
		servlet = spy(servlet);
		servlet.doPut(req, resp);
		verify(servlet).applyRoute(RouteType.PUT, req, resp);
	}

	@Test
	public void shouldApplyDeleteRouteWhenDoDelete() throws ServletException, IOException {
		MockHttpServletRequest req = new MockHttpServletRequest();
		servlet = spy(servlet);
		servlet.doDelete(req, resp);
		verify(servlet).applyRoute(RouteType.DELETE, req, resp);
	}

	@Test
	public void shouldFindViewForResolvedRoute() throws ServletException, IOException {
		viewResolverRegistry.addResolver(String.class, new ViewResolver<String>() {
			@Override
			public void resolve(HttpServletRequest req, HttpServletResponse resp, String viewResult) {
				resp.setStatus(123);
			}
		});

		servlet.applyRoute(RouteType.GET, new MockHttpServletRequest("/get/"), resp);
		assertThat(resp.status(), is(123));
	}

	@Test
	public void shouldNotResolveViewWhenNullViewResultReturned() throws ServletException, IOException {
		when(routes.invoke(anyString(), Mockito.any(RouteType.class), Mockito.any(HttpServletRequest.class), Mockito.any(HttpServletResponse.class))).thenReturn(null);

		viewResolverRegistry.addResolver(Object.class, new ViewResolver<Object>() {
			@Override
			public void resolve(HttpServletRequest req, HttpServletResponse resp, Object viewResult) {
				resp.setStatus(123);
			}
		});

		servlet.applyRoute(RouteType.GET, new MockHttpServletRequest("/get/"), resp);
		assertThat(resp.status(), is(-1));
	}

	@Test
	public void shouldCatchExceptionsFromViewResolversAndResolveExceptionWithExceptionView() throws ServletException, IOException {
		when(routes.invoke(anyString(), Mockito.any(RouteType.class), Mockito.any(HttpServletRequest.class), Mockito.any(HttpServletResponse.class))).thenReturn("View Name");

		viewResolverRegistry.addResolver(String.class, new ViewResolver<String>() {
			@Override
			public void resolve(HttpServletRequest req, HttpServletResponse resp, String viewResult) {
				throw new RuntimeException("Intentional Exception");
			}
		});
		viewResolverRegistry.addResolver(Exception.class, new ViewResolver<Exception>() {
			@Override
			public void resolve(HttpServletRequest req, HttpServletResponse resp, Exception viewResult) {
				resp.setStatus(5678);
			}
		});

		servlet.applyRoute(RouteType.GET, new MockHttpServletRequest("/get/"), resp);
		assertThat(resp.status(), is(5678));
	}

	@Test
	public void shouldCatchExceptionsFromViewResolversButDoNothingWhenResponseAlreadyCommitted() throws ServletException, IOException {
		when(routes.invoke(anyString(), Mockito.any(RouteType.class), Mockito.any(HttpServletRequest.class), Mockito.any(HttpServletResponse.class))).thenThrow(new RuntimeException("Expected exception"));

		viewResolverRegistry.addResolver(Exception.class, new ViewResolver<Exception>() {
			@Override
			public void resolve(HttpServletRequest req, HttpServletResponse resp, Exception viewResult) {
				resp.setStatus(5678);
			}
		});

		resp.sendError(1234);
		servlet.applyRoute(RouteType.GET, new MockHttpServletRequest("/get/"), resp);
		assertThat(resp.status(), is(1234));
	}

	private void setInjectionContextIntoServlet(UpdatableInjectionContext injectionContext) {
		TestSupport.setField(servlet, "injectionContext", injectionContext);
	}

	private UpdatableInjectionContext getInjectionContextFromServlet(WebFrameworkServlet servlet) {
		return TestSupport.getField(servlet, "injectionContext");
	}
}
