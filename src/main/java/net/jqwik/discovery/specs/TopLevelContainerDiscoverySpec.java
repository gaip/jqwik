package net.jqwik.discovery.specs;

import net.jqwik.api.Group;
import net.jqwik.discovery.predicates.IsPotentialTestContainer;
import net.jqwik.discovery.predicates.IsTopLevelClass;

import java.util.function.Predicate;

import static net.jqwik.support.JqwikReflectionSupport.isStatic;

public class TopLevelContainerDiscoverySpec implements DiscoverySpec<Class<?>> {

	private final static Predicate<Class<?>> isPotentialTestContainer = new IsPotentialTestContainer();
	private final static Predicate<Class<?>> isTopLevelClass = new IsTopLevelClass();
	private final static Predicate<Class<?>> isStaticNonGroupMember = candidate -> isStatic(candidate) && !candidate.isAnnotationPresent(Group.class);

	@Override
	public boolean shouldBeDiscovered(Class<?> candidate) {
		return isPotentialTestContainer
				.and(isTopLevelClass.or(isStaticNonGroupMember))
				.test(candidate);
	}

	@Override
	public boolean butSkippedOnExecution(Class<?> candidate) {
		return false;
	}

	@Override
	public String skippingReason(Class<?> candidate) {
		return null;
	}
}
