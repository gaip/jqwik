package net.jqwik.properties.newShrinking;

import net.jqwik.api.*;
import net.jqwik.properties.newShrinking.ShrinkableTypesForTest.*;

import java.util.concurrent.atomic.*;
import java.util.function.*;

import static org.assertj.core.api.Assertions.*;

class DeepSearchShrinkingTests {

	private AtomicInteger counter = new AtomicInteger(0);
	private Runnable count = counter::incrementAndGet;

	@Group
	class ShrinkableWithOneStepShrinking {

		@Example
		void shrinkDownAllTheWay() {
			NShrinkable<Integer> shrinkable = new OneStepShrinkable(5);
			assertThat(shrinkable.value()).isEqualTo(5);
			assertThat(shrinkable.distance()).isEqualTo(ShrinkingDistance.of(5));

			ShrinkingSequence<Integer> sequence = shrinkable.shrink(anInt -> false);
			assertThat(sequence.current()).isEqualTo(shrinkable);

			assertThat(sequence.next(count)).isTrue();
			assertThat(sequence.current().value()).isEqualTo(4);
			assertThat(sequence.next(count)).isTrue();
			assertThat(sequence.current().value()).isEqualTo(3);
			assertThat(sequence.next(count)).isTrue();
			assertThat(sequence.current().value()).isEqualTo(2);
			assertThat(sequence.next(count)).isTrue();
			assertThat(sequence.current().value()).isEqualTo(1);
			assertThat(sequence.next(count)).isTrue();
			assertThat(sequence.current().value()).isEqualTo(0);
			assertThat(sequence.next(count)).isFalse();
			assertThat(sequence.current().value()).isEqualTo(0);

			assertThat(counter.get()).isEqualTo(5);
		}

		@Example
		void shrinkDownSomeWay() {
			NShrinkable<Integer> shrinkable = new OneStepShrinkable(5);
			assertThat(shrinkable.value()).isEqualTo(5);
			assertThat(shrinkable.distance()).isEqualTo(ShrinkingDistance.of(5));

			ShrinkingSequence<Integer> sequence = shrinkable.shrink(anInt -> anInt < 2);

			assertThat(sequence.next(count)).isTrue();
			assertThat(sequence.current().value()).isEqualTo(4);
			assertThat(sequence.next(count)).isTrue();
			assertThat(sequence.current().value()).isEqualTo(3);
			assertThat(sequence.next(count)).isTrue();
			assertThat(sequence.current().value()).isEqualTo(2);
			assertThat(sequence.next(count)).isFalse();
			assertThat(sequence.current().value()).isEqualTo(2);

			assertThat(counter.get()).isEqualTo(3);
		}

		@Example
		void shrinkDownWithFilter() {
			NShrinkable<Integer> shrinkable = new OneStepShrinkable(10);

			Falsifier<Integer> falsifier = anInt -> anInt < 6;
			Predicate<Integer> onlyEvenNumbers = anInt -> anInt % 2 == 0;
			ShrinkingSequence<Integer> sequence = shrinkable.shrink(falsifier.withFilter(onlyEvenNumbers));

			assertThat(sequence.next(count)).isTrue();
			assertThat(sequence.current().value()).isEqualTo(10);
			assertThat(sequence.next(count)).isTrue();
			assertThat(sequence.current().value()).isEqualTo(8);
			assertThat(sequence.next(count)).isTrue();
			assertThat(sequence.current().value()).isEqualTo(8);
			assertThat(sequence.next(count)).isTrue();
			assertThat(sequence.current().value()).isEqualTo(6);
			assertThat(sequence.next(count)).isTrue();
			assertThat(sequence.current().value()).isEqualTo(6);
			assertThat(sequence.next(count)).isFalse();

			assertThat(counter.get()).isEqualTo(5);
		}

	}

	@Group
	class ShrinkableWithFullShrinking {
		@Example
		void shrinkDownAllTheWay() {
			NShrinkable<Integer> shrinkable = new FullShrinkable(5);

			ShrinkingSequence<Integer> sequence = shrinkable.shrink(anInt -> false);
			assertThat(sequence.current()).isEqualTo(shrinkable);

			assertThat(sequence.next(count)).isTrue();
			assertThat(sequence.current().value()).isEqualTo(0);
			assertThat(sequence.next(count)).isFalse();
			assertThat(sequence.current().value()).isEqualTo(0);

			assertThat(counter.get()).isEqualTo(1);
		}

		@Example
		void shrinkDownSomeWay() {
			NShrinkable<Integer> shrinkable = new FullShrinkable(5);
			assertThat(shrinkable.value()).isEqualTo(5);
			assertThat(shrinkable.distance()).isEqualTo(ShrinkingDistance.of(5));

			ShrinkingSequence<Integer> sequence = shrinkable.shrink(anInt -> anInt < 2);

			assertThat(sequence.next(count)).isTrue();
			assertThat(sequence.current().value()).isEqualTo(2);
			assertThat(sequence.next(count)).isFalse();
			assertThat(sequence.current().value()).isEqualTo(2);

			assertThat(counter.get()).isEqualTo(1);
		}

		@Example
		void shrinkDownWithFilter() {
			NShrinkable<Integer> shrinkable = new FullShrinkable(10);

			Falsifier<Integer> falsifier = anInt -> anInt < 6;
			Predicate<Integer> onlyEvenNumbers = anInt -> anInt % 2 == 0;
			ShrinkingSequence<Integer> sequence = shrinkable.shrink(falsifier.withFilter(onlyEvenNumbers));

			assertThat(sequence.next(count)).isTrue();
			assertThat(sequence.current().value()).isEqualTo(6);
			assertThat(sequence.next(count)).isFalse();

			assertThat(counter.get()).isEqualTo(2);
		}

	}

	@Group
	class ShrinkableWithPartialShrinking {
		@Example
		void shrinkDownAllTheWay() {
			NShrinkable<Integer> shrinkable = new PartialShrinkable(5);

			ShrinkingSequence<Integer> sequence = shrinkable.shrink(anInt -> false);
			assertThat(sequence.current()).isEqualTo(shrinkable);

			assertThat(sequence.next(count)).isTrue();
			assertThat(sequence.current().value()).isEqualTo(3);
			assertThat(sequence.next(count)).isTrue();
			assertThat(sequence.current().value()).isEqualTo(1);
			assertThat(sequence.next(count)).isTrue();
			assertThat(sequence.current().value()).isEqualTo(0);
			assertThat(sequence.next(count)).isFalse();
			assertThat(sequence.current().value()).isEqualTo(0);

			assertThat(counter.get()).isEqualTo(3);
		}

		@Example
		void shrinkDownSomeWay() {
			NShrinkable<Integer> shrinkable = new PartialShrinkable(5);

			ShrinkingSequence<Integer> sequence = shrinkable.shrink(anInt -> anInt < 2);

			assertThat(sequence.next(count)).isTrue();
			assertThat(sequence.current().value()).isEqualTo(3);
			assertThat(sequence.next(count)).isTrue();
			assertThat(sequence.current().value()).isEqualTo(2);
			assertThat(sequence.next(count)).isFalse();
			assertThat(sequence.current().value()).isEqualTo(2);

			assertThat(counter.get()).isEqualTo(2);
		}

		@Example
		void shrinkDownWithFilter() {
			NShrinkable<Integer> shrinkable = new PartialShrinkable(10);

			Falsifier<Integer> falsifier = anInt -> anInt < 6;
			Predicate<Integer> onlyEvenNumbers = anInt -> anInt % 2 == 0;
			ShrinkingSequence<Integer> sequence = shrinkable.shrink(falsifier.withFilter(onlyEvenNumbers));

			assertThat(sequence.next(count)).isTrue();
			assertThat(sequence.current().value()).isEqualTo(8);
			assertThat(sequence.next(count)).isTrue();
			assertThat(sequence.current().value()).isEqualTo(6);
			assertThat(sequence.next(count)).isTrue();
			assertThat(sequence.next(count)).isTrue();
			assertThat(sequence.next(count)).isTrue();
			assertThat(sequence.current().value()).isEqualTo(6);
			assertThat(sequence.next(count)).isFalse();

			assertThat(counter.get()).isEqualTo(5);
		}

	}

}
