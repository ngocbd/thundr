package com.threewks.thundr.action.method.bind.request;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;

import com.threewks.thundr.introspection.ParameterDescription;
import com.threewks.thundr.test.mock.servlet.MockHttpServletRequest;
import com.threewks.thundr.test.mock.servlet.MockHttpServletResponse;

public class RequestClassBinderTest {
	private RequestClassBinder binder;
	private MockHttpServletRequest request = new MockHttpServletRequest();
	private MockHttpServletResponse response = new MockHttpServletResponse();
	private HttpSession session;
	private Map<String, String> pathVariables;
	private Map<ParameterDescription, Object> parameterDescriptions;

	@Before
	public void before() {
		binder = new RequestClassBinder();
		parameterDescriptions = new LinkedHashMap<ParameterDescription, Object>();
		pathVariables = new HashMap<String, String>();
		session = request.getSession();
	}

	@Test
	public void shouldBindToHttpServletRequestParam() {
		ParameterDescription httpServletRequestParameter = new ParameterDescription("request", HttpServletRequest.class);
		parameterDescriptions.put(httpServletRequestParameter, null);

		binder.bindAll(parameterDescriptions, request, response, pathVariables);
		assertThat(parameterDescriptions.get(httpServletRequestParameter), is((Object) request));
	}

	@Test
	public void shouldBindToServletRequestParam() {
		ParameterDescription servletRequestParameter = new ParameterDescription("request", ServletRequest.class);
		parameterDescriptions.put(servletRequestParameter, null);

		binder.bindAll(parameterDescriptions, request, response, pathVariables);
		assertThat(parameterDescriptions.get(servletRequestParameter), is((Object) request));
	}

	@Test
	public void shouldBindToHttpServletResponseParam() {
		ParameterDescription httpServletResponseParameter = new ParameterDescription("response", HttpServletResponse.class);
		parameterDescriptions.put(httpServletResponseParameter, null);

		binder.bindAll(parameterDescriptions, request, response, pathVariables);
		assertThat(parameterDescriptions.get(httpServletResponseParameter), is((Object) response));
	}

	@Test
	public void shouldBindToServletResponseParam() {
		ParameterDescription servletResponseParameter = new ParameterDescription("response", ServletResponse.class);
		parameterDescriptions.put(servletResponseParameter, null);

		binder.bindAll(parameterDescriptions, request, response, pathVariables);
		assertThat(parameterDescriptions.get(servletResponseParameter), is((Object) response));
	}

	@Test
	public void shouldBindToHttpSessionParam() {
		ParameterDescription sessionParameter = new ParameterDescription("session", HttpSession.class);
		parameterDescriptions.put(sessionParameter, null);

		binder.bindAll(parameterDescriptions, request, response, pathVariables);
		assertThat(parameterDescriptions.get(sessionParameter), is((Object) session));
	}

	@Test
	public void shouldNotBindToRequestClassesWhenNotNull() {
		ParameterDescription servletRequestParameter = new ParameterDescription("request", ServletRequest.class);
		ParameterDescription servletResponseParameter = new ParameterDescription("response", ServletResponse.class);
		ParameterDescription sessionParameter = new ParameterDescription("session", HttpSession.class);

		parameterDescriptions.put(servletRequestParameter, "string");
		parameterDescriptions.put(servletResponseParameter, "string");
		parameterDescriptions.put(sessionParameter, "string");

		binder.bindAll(parameterDescriptions, request, response, pathVariables);
		assertThat(parameterDescriptions.get(servletRequestParameter), is((Object) "string"));
		assertThat(parameterDescriptions.get(servletResponseParameter), is((Object) "string"));
		assertThat(parameterDescriptions.get(sessionParameter), is((Object) "string"));
	}

}
