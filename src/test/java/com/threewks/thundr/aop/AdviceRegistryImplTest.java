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
package com.threewks.thundr.aop;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.spy;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.Test;

public class AdviceRegistryImplTest {
	private AdviceRegistryImpl registry = new AdviceRegistryImpl();

	@Test
	public void shouldRegisterPointcutAndAdvice() {
		assertThat(registry.contains(Pointcut.class), is(false));
		assertThat(registry.contains(Pointcut.class, TestAdvice.class), is(false));
		assertThat(registry.contains(Pointcut.class, TestAdvice2.class), is(false));

		registry.add(Pointcut.class, new TestAdvice());
		assertThat(registry.contains(Pointcut.class), is(true));
		assertThat(registry.contains(Pointcut.class, TestAdvice.class), is(true));
		assertThat(registry.contains(Pointcut.class, TestAdvice2.class), is(false));

		registry.add(Pointcut.class, new TestAdvice2());
		assertThat(registry.contains(Pointcut.class), is(true));
		assertThat(registry.contains(Pointcut.class, TestAdvice.class), is(false));
		assertThat(registry.contains(Pointcut.class, TestAdvice2.class), is(true));

		registry.remove(Pointcut.class);
		assertThat(registry.contains(Pointcut.class), is(false));
		assertThat(registry.contains(Pointcut.class, TestAdvice.class), is(false));
		assertThat(registry.contains(Pointcut.class, TestAdvice2.class), is(false));
	}

	@Test
	public void shouldNotFailProxyingANullInstance() {
		assertThat(registry.proxyIfNeeded(null), is(nullValue()));
	}

	@Test
	public void shouldNotFailProxyingAnAlreadyProxiedInstance() {
		registry.add(Pointcut.class, new BaseAdvice<Pointcut, Integer>());

		SimpleService alreadyProxiedService = spy(new SimpleService());
		assertThat(alreadyProxiedService.getClass().equals(SimpleService.class), is(false));

		SimpleService resultingService = registry.proxyIfNeeded(alreadyProxiedService);
		assertThat(resultingService, is(notNullValue()));
		assertThat(resultingService, is(sameInstance(alreadyProxiedService)));
	}

	@Test
	public void shouldOnlyProxyIfAPointcutIsRegistered() {
		SimpleService simpleService = registry.proxyIfNeeded(new SimpleService());
		assertThat(simpleService.getClass() == SimpleService.class, is(true));

		registry.add(PointcutUnused.class, new BaseAdvice<PointcutUnused, Integer>() {
		});

		SimpleService unenhancedSimpleService = registry.proxyIfNeeded(new SimpleService());
		assertThat(unenhancedSimpleService.getClass() == SimpleService.class, is(true));

		registry.add(Pointcut.class, new BaseAdvice<Pointcut, Integer>() {
		});

		SimpleService enhancedService = registry.proxyIfNeeded(new SimpleService());
		assertThat(enhancedService.getClass() == SimpleService.class, is(false));
	}

	@Test
	public void shouldCreateProxyAndExecuteBeforeAdviceOnMethodCall() {
		registry.add(Pointcut.class, new BaseAdvice<Pointcut, Integer>() {
			@Override
			public Integer before(Pointcut annotation, Arguments arguments) {
				arguments.replaceArgument("numberString", annotation.value());
				return null;
			}
		});

		assertThat(registry.contains(Pointcut.class), is(true));

		SimpleService simpleService = registry.proxyIfNeeded(new SimpleService());

		assertThat(simpleService.method("2"), is(1));
	}

	@Test
	public void shouldCreateProxyAndExecuteAfterAdviceOnMethodCall() {
		registry.add(Pointcut.class, new BaseAdvice<Pointcut, Integer>() {
			@Override
			public Integer after(Integer result, Pointcut annotation, Arguments arguments) {
				assertThat(result, is(2));
				return 3;
			}
		});

		assertThat(registry.contains(Pointcut.class), is(true));

		SimpleService simpleService = registry.proxyIfNeeded(new SimpleService());

		assertThat(simpleService.method("2"), is(3));
	}

	@Test
	public void shouldCreateProxyAndExecuteExceptionAdviceOnMethodCall() {
		registry.add(Pointcut.class, new BaseAdvice<Pointcut, Integer>() {
			@Override
			public Integer exception(Exception e, Pointcut annotation, Arguments arguments) {
				assertThat(e.getMessage(), is("Expected"));
				return -1;
			}

			@Override
			public Integer after(Integer result, Pointcut annotation, Arguments arguments) {
				throw new RuntimeException("Expected");
			}
		});

		assertThat(registry.contains(Pointcut.class), is(true));
		SimpleService simpleService = registry.proxyIfNeeded(new SimpleService());

		assertThat(simpleService.method("2"), is(-1));
	}

	@Test
	public void shouldCreateProxyAndExecuteBeforeAdviceOnSuperClassMethodCall() {
		registry.add(Pointcut.class, new BaseAdvice<Pointcut, Integer>() {
			@Override
			public Integer before(Pointcut annotation, Arguments arguments) {
				arguments.replaceArgument("numberString", annotation.value());
				return null;
			}
		});

		assertThat(registry.contains(Pointcut.class), is(true));

		SimpleServiceExtension simpleServiceExtension = registry.proxyIfNeeded(new SimpleServiceExtension());
		SimpleService simpleService = simpleServiceExtension;
		assertThat(simpleServiceExtension.method("2"), is(1));
		assertThat(simpleService.method("2"), is(1));
	}

	@Test
	public void shouldCreateProxyAndExecuteAfterAdviceOnSuperClassMethodCall() {
		registry.add(Pointcut.class, new BaseAdvice<Pointcut, Integer>() {
			@Override
			public Integer after(Integer result, Pointcut annotation, Arguments arguments) {
				assertThat(result, is(2));
				return 3;
			}
		});

		assertThat(registry.contains(Pointcut.class), is(true));

		SimpleServiceExtension simpleServiceExtension = registry.proxyIfNeeded(new SimpleServiceExtension());
		SimpleService simpleService = simpleServiceExtension;
		assertThat(simpleServiceExtension.method("2"), is(3));
		assertThat(simpleService.method("2"), is(3));
	}

	@Test
	public void shouldCreateProxyAndExecuteExceptionAdviceOnSuperClassMethodCall() {
		registry.add(Pointcut.class, new BaseAdvice<Pointcut, Integer>() {
			@Override
			public Integer exception(Exception e, Pointcut annotation, Arguments arguments) {
				assertThat(e.getMessage(), is("Expected"));
				return -1;
			}

			@Override
			public Integer after(Integer result, Pointcut annotation, Arguments arguments) {
				throw new RuntimeException("Expected");
			}
		});

		assertThat(registry.contains(Pointcut.class), is(true));

		SimpleServiceExtension simpleServiceExtension = registry.proxyIfNeeded(new SimpleServiceExtension());
		SimpleService simpleService = simpleServiceExtension;
		assertThat(simpleServiceExtension.method("2"), is(-1));
		assertThat(simpleService.method("2"), is(-1));
	}

	@Test
	public void shouldRunBeforeInOrderOfAnnotation() {

		registry.add(Pointcut.class, new BaseAdvice<Pointcut, Integer>() {
			@Override
			public Integer before(Pointcut annotation, Arguments arguments) {
				assertThat(arguments.getArgument("input"), is("0"));
				arguments.replaceArgument("input", "1");
				return null;
			}
		});
		registry.add(Pointcut2.class, new BaseAdvice<Pointcut2, Integer>() {
			@Override
			public Integer before(Pointcut2 annotation, Arguments arguments) {
				assertThat(arguments.getArgument("input"), is("1"));
				arguments.replaceArgument("input", "2");
				return null;
			}
		});
		SimpleService simpleService = registry.proxyIfNeeded(new SimpleService());

		assertThat(simpleService.method2("0"), is("2"));
	}

	@Test
	public void shouldRunAfterInReverseOrderOfAnnotation() {
		registry.add(Pointcut.class, new BaseAdvice<Pointcut, String>() {
			@Override
			public String after(String result, Pointcut annotation, Arguments arguments) {
				assertThat(result, is("2"));
				return "1";
			}
		});
		registry.add(Pointcut2.class, new BaseAdvice<Pointcut2, String>() {
			@Override
			public String after(String result, Pointcut2 annotation, Arguments arguments) {
				assertThat(result, is("0"));
				return "2";
			}
		});
		SimpleService simpleService = registry.proxyIfNeeded(new SimpleService());

		assertThat(simpleService.method2("0"), is("1"));
	}

	@Test
	public void shouldRunExceptionInReverseOrderOfAnnotation() {
		registry.add(Pointcut.class, new BaseAdvice<Pointcut, String>() {
			@Override
			public String exception(Exception e, Pointcut annotation, Arguments arguments) {
				return "1";
			}
		});
		registry.add(Pointcut2.class, new BaseAdvice<Pointcut2, String>() {
			@Override
			public String after(String result, Pointcut2 annotation, Arguments arguments) {
				return "2";
			}
		});
		SimpleService simpleService = registry.proxyIfNeeded(new SimpleService());
		assertThat(simpleService.method2("0"), is("2"));
	}

	protected static class SimpleService {
		@Pointcut("1")
		public int method(String numberString) {
			return Integer.parseInt(numberString);
		}

		@Pointcut("1")
		@Pointcut2("2")
		public String method2(String input) {
			return input;
		}
	}

	protected static class SimpleServiceExtension extends SimpleService {
	}

	@Retention(RetentionPolicy.RUNTIME)
	protected static @interface Pointcut {
		public String value();
	}

	@Retention(RetentionPolicy.RUNTIME)
	protected static @interface Pointcut2 {
		public String value();
	}

	@Retention(RetentionPolicy.RUNTIME)
	protected static @interface PointcutUnused {
	}

	protected static class TestAdvice extends BaseAdvice<Pointcut, Integer> {

	}

	protected static class TestAdvice2 extends BaseAdvice<Pointcut, Integer> {

	}
}
