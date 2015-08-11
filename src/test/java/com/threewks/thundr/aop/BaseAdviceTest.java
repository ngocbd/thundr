package com.threewks.thundr.aop;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.lang.annotation.Annotation;

import org.junit.Test;

public class BaseAdviceTest {
	private BaseAdvice<Annotation, Object> baseAdvice = new BaseAdvice<>();

	@Test
	public void shouldReturnNullForBefore() {
		assertThat(baseAdvice.before(null, null), is(nullValue()));
	}

	@Test
	public void shouldReturnGivenResultForAfter() {
		assertThat(baseAdvice.after("Result", null, null), is("Result"));
	}

	@Test
	public void shouldReturnNullForException() {
		assertThat(baseAdvice.exception(null, null, null), is(nullValue()));
	}

}
