package net.jqwik.engine.properties.shrinking;

import java.util.*;
import java.util.stream.*;

import org.assertj.core.api.*;

import net.jqwik.api.*;
import net.jqwik.engine.execution.lifecycle.*;
import net.jqwik.engine.support.*;

@Group
public class ShrinkableTypesForTest {

	public static class OneStepShrinkable extends AbstractValueShrinkable<Integer> {
		private final int minimum;
		private final int maximum;

		public OneStepShrinkable(int integer) {
			this(integer, 0, 100);
		}

		public OneStepShrinkable(int integer, int minimum, int maximum) {
			super(integer);
			this.minimum = minimum;
			this.maximum = maximum;
		}

		@Override
		public Stream<Shrinkable<Integer>> shrink() {
			if (this.value() == minimum)
				return Stream.empty();
			int integer = this.value() - 1;
			return Stream.of(createShrinkable(integer));
		}

		protected OneStepShrinkable createShrinkable(int integer) {
			return new OneStepShrinkable(integer, minimum, maximum);
		}

		@Override
		public Stream<Shrinkable<Integer>> grow() {
			if (value() >= maximum) {
				return Stream.empty();
			}
			return Stream.of(createShrinkable(this.value() + 1));
		}

		@Override
		public Optional<Shrinkable<Integer>> grow(Shrinkable<?> before, Shrinkable<?> after) {
			Object beforeValue = before.value();
			Object afterValue = after.value();
			if (beforeValue instanceof Integer && afterValue instanceof Integer) {
				int diff = (int) beforeValue - (int) afterValue;
				int grownValue = value() + diff;
				if (grownValue >= minimum) {
					return Optional.of(createShrinkable(grownValue));
				}
			}
			return Optional.empty();
		}

		@Override
		public ShrinkingDistance distance() {
			return ShrinkingDistance.of(value() - minimum);
		}

	}

	public static class SlowShrinkable extends OneStepShrinkable {

		public SlowShrinkable(int integer) {
			super(integer);
		}

		@Override
		public Stream<Shrinkable<Integer>> shrink() {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return super.shrink();
		}

		protected OneStepShrinkable createShrinkable(int integer) {
			return new SlowShrinkable(integer);
		}

	}

	public static class ShrinkableUsingCurrentTestDescriptor extends OneStepShrinkable {

		public ShrinkableUsingCurrentTestDescriptor(int integer) {
			super(integer);
		}

		@Override
		public Stream<Shrinkable<Integer>> shrink() {
			Assertions.assertThat(CurrentTestDescriptor.get()).isNotNull();
			return super.shrink();
		}

		protected OneStepShrinkable createShrinkable(int integer) {
			return new ShrinkableUsingCurrentTestDescriptor(integer);
		}

	}

	public static class FullShrinkable extends AbstractValueShrinkable<Integer> {
		private final int max;

		FullShrinkable(int integer, int max) {
			super(integer);
			this.max = max;
		}

		FullShrinkable(int integer) {
			this(integer, 100);
		}

		@Override
		public Stream<Shrinkable<Integer>> shrink() {
			return IntStream.range(0, this.value()).mapToObj(FullShrinkable::new);
		}

		@Override
		public Stream<Shrinkable<Integer>> grow() {
			return IntStream.range(value() + 1, max).mapToObj(i -> new FullShrinkable(i, max));
		}

		@Override
		public ShrinkingDistance distance() {
			return ShrinkingDistance.of(value());
		}

	}

	public static class PartialShrinkable extends AbstractValueShrinkable<Integer> {
		PartialShrinkable(int integer) {
			super(integer);
		}

		@Override
		public Stream<Shrinkable<Integer>> shrink() {
			Integer value = this.value();
			List<Stream<Shrinkable<Integer>>> shrinks = new ArrayList<>();
			if (value > 0) shrinks.add(Stream.of(new PartialShrinkable(value - 1)));
			if (value > 1) shrinks.add(Stream.of(new PartialShrinkable(value - 2)));
			return JqwikStreamSupport.concat(shrinks);
		}

		@Override
		public ShrinkingDistance distance() {
			return ShrinkingDistance.of(value());
		}
	}


	public static class ShrinkToLarger extends AbstractValueShrinkable<Integer> {

		ShrinkToLarger(int integer) {
			super(integer);
		}

		@Override
		public Stream<Shrinkable<Integer>> shrink() {
			return Stream.of(new ShrinkToLarger(this.value() + 1));
		}

		@Override
		public ShrinkingDistance distance() {
			return ShrinkingDistance.of(value());
		}

	}

	public static class ShrinkWithFixedDistance extends AbstractValueShrinkable<Integer> {

		ShrinkWithFixedDistance(int integer) {
			super(integer);
		}

		@Override
		public Stream<Shrinkable<Integer>> shrink() {
			if (this.value() == 0) {
				return Stream.empty();
			}
			return Stream.of(new ShrinkWithFixedDistance(this.value() - 1));
		}

		@Override
		public ShrinkingDistance distance() {
			return ShrinkingDistance.of(42);
		}

	}
}
