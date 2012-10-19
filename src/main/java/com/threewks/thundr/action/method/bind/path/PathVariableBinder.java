package com.threewks.thundr.action.method.bind.path;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jodd.typeconverter.TypeConverterManager;

import com.threewks.thundr.action.method.bind.ActionMethodBinder;
import com.threewks.thundr.introspection.ParameterDescription;

public class PathVariableBinder implements ActionMethodBinder {

	public static final List<Class<?>> PathVariableTypes = Arrays.<Class<?>>
			asList(String.class, int.class, Integer.class, double.class, Double.class, long.class, Long.class, short.class,
					Short.class, float.class, Float.class, BigDecimal.class, BigInteger.class);

	@Override
	public void bindAll(Map<ParameterDescription, Object> bindings, HttpServletRequest req, HttpServletResponse resp, Map<String, String> pathVariables) {
		for (ParameterDescription parameterDescription : bindings.keySet()) {
			if (bindings.get(parameterDescription) == null) {
				if (canBindFromPathVariable(parameterDescription)) {
					Object value = bind(parameterDescription, pathVariables);
					bindings.put(parameterDescription, value);
				}
			}
		}
	}

	private boolean canBindFromPathVariable(ParameterDescription parameterDescription) {
		return PathVariableTypes.contains(parameterDescription.type());
	}

	private Object bind(ParameterDescription parameterDescription, Map<String, String> pathVariables) {
		String value = pathVariables.get(parameterDescription.name());
		return TypeConverterManager.lookup(parameterDescription.classType()).convert(value);
	}
}
