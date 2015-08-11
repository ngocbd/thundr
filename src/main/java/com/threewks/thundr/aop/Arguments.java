package com.threewks.thundr.aop;

import java.util.List;

public interface Arguments {
	public List<Object> getArguments();

	public <T> T getArgument(Class<T> type);

	public <T> T getArgument(String name);

	public <T> T getArgument(Class<T> type, String name);

	public <T> void replaceArgument(Class<T> type, T value);

	public <T> void replaceArgument(String name, T value);

	public <T> void replaceArgument(Class<T> type, String name, T value);

	public Object[] toArgs();
}
