package net.jqwik.properties.arbitraries.exhaustive;

import java.util.*;

import net.jqwik.api.*;
import net.jqwik.support.*;

class SetExhaustiveGenerator<T> implements ExhaustiveGenerator<Set<T>> {
	private final Arbitrary<T> elementArbitrary;
	private final long maxCount;
	private final int minSize;
	private final int maxSize;

	static Optional<Long> calculateMaxCount(Arbitrary<?> elementArbitrary, int minSize, int maxSize) {
		Optional<? extends ExhaustiveGenerator<?>> exhaustiveElement = elementArbitrary.exhaustive();
		if (!exhaustiveElement.isPresent())
			return Optional.empty();

		long elementMaxCount = exhaustiveElement.get().maxCount();
		long sum = 0;
		for (int n = minSize; n <= maxSize; n++) {
			if (n == 0) { // empty set
				sum += 1;
				continue;
			}
			if (elementMaxCount < n) { // empty set
				continue;
			}
			long choices = factorial(elementMaxCount) / (factorial(elementMaxCount - n) * factorial(n));
			if (choices > Integer.MAX_VALUE || choices < 0) { // Stop when break off point reached
				return Optional.empty();
			}
			sum += choices;
		}
		return Optional.of(sum);
	}

	private static long factorial(long number) {
		long result = 1;

		for (long factor = 2; factor <= number; factor++) {
			result *= factor;
		}

		return result;
	}

	SetExhaustiveGenerator(Arbitrary<T> elementArbitrary, long maxCount, int minSize, int maxSize) {
		this.elementArbitrary = elementArbitrary;
		this.minSize = minSize;
		this.maxSize = maxSize;
		this.maxCount = maxCount;
	}

	@Override
	public Iterator<Set<T>> iterator() {
		return Combinatorics.setCombinations(elementArbitrary.exhaustive().get(), minSize, maxSize);
	}

	@Override
	public long maxCount() {
		return maxCount;
	}
}
