package net.jqwik.engine.execution.reporting;

import java.util.*;

import net.jqwik.api.*;

public class NullReportingFormat implements SampleReportingFormat {
	@Override
	// Never used
	public boolean appliesTo(Object value) {
		return true;
	}

	@Override
	public Object report(Object value) {
		return value;
	}

	@Override
	public Optional<String> label(Object value) {
		return Optional.empty();
	}
}
