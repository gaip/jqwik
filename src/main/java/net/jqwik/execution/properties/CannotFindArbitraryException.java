package net.jqwik.execution.properties;

import net.jqwik.JqwikException;
import net.jqwik.api.properties.ForAll;

import java.lang.reflect.Parameter;

public class CannotFindArbitraryException extends JqwikException {

	private final Parameter parameter;

	CannotFindArbitraryException(Parameter parameter) {
		super(createMessage(parameter));
		this.parameter = parameter;
	}

	private static String createMessage(Parameter parameter) {
		String forAllValue = parameter.getDeclaredAnnotation(ForAll.class).value();
		if (forAllValue.isEmpty())
			return String.format("Cannot find an Arbitrary for Parameter of type [%s]", parameter.getType());
		else
			return String.format("Cannot find an Arbitrary [%s] for Parameter of type [%s]", forAllValue, parameter.getType());
	}

	public Parameter getParameter() {
		return parameter;
	}
}
