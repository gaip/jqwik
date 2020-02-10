package net.jqwik.engine;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.*;

import org.junit.platform.engine.*;

import net.jqwik.api.*;
import net.jqwik.api.lifecycle.*;
import net.jqwik.engine.descriptor.*;
import net.jqwik.engine.execution.lifecycle.*;
import net.jqwik.engine.support.*;

import static net.jqwik.engine.support.JqwikReflectionSupport.*;

public class TestHelper {
	public static List<MethodParameter> getParametersFor(Class<?> aClass, String methodName) {
		return getParameters(getMethod(aClass, methodName), aClass);
	}

	private static List<MethodParameter> getParameters(Method method, Class<?> containerClass) {
		return Arrays.stream(getMethodParameters(method, containerClass)).collect(Collectors.toList());
	}

	public static Method getMethod(Class<?> aClass, String methodName) {
		return Arrays.stream(aClass.getDeclaredMethods()).filter(m -> m.getName().equals(methodName)).findFirst().get();
	}

	public static PropertyMethodDescriptor createPropertyMethodDescriptor(
		Class<?> containerClass, String methodName, String seed, int tries, int maxDiscardRatio, ShrinkingMode shrinking
	) {
		UniqueId uniqueId = UniqueId.root("test", "i dont care");
		Method method = getMethod(containerClass, methodName);
		PropertyConfiguration propertyConfig = new PropertyConfiguration("Property", seed, null, null, tries, maxDiscardRatio, shrinking, GenerationMode.AUTO, AfterFailureMode.PREVIOUS_SEED);
		return new PropertyMethodDescriptor(uniqueId, method, containerClass, propertyConfig);
	}

	public static LifecycleHooksSupplier emptyLifecycleSupplier() {
		return lifecycleSupplier(Collections.emptyList());
	}

	public static LifecycleHooksSupplier lifecycleSupplier(List<AroundPropertyHook> aroundPropertyHooks) {
		return new LifecycleHooksSupplier() {
			@Override
			public AroundPropertyHook aroundPropertyHook(PropertyMethodDescriptor propertyMethodDescriptor) {
				return HookSupport.combineAroundPropertyHooks(aroundPropertyHooks);
			}

			@Override
			public AroundTryHook aroundTryHook(PropertyMethodDescriptor methodDescriptor) {
				return AroundTryHook.BASE;
			}

			@Override
			public BeforeContainerHook beforeContainerHook(TestDescriptor descriptor) {
				return BeforeContainerHook.DO_NOTHING;
			}

			@Override
			public AfterContainerHook afterContainerHook(TestDescriptor descriptor) {
				return AfterContainerHook.DO_NOTHING;
			}

			@Override
			public SkipExecutionHook skipExecutionHook(TestDescriptor testDescriptor) {
				return descriptor -> SkipExecutionHook.SkipResult.doNotSkip();
			}
		};
	}

	public static List<MethodParameter> getParameters(PropertyMethodDescriptor methodDescriptor) {
		return
			Arrays
				.stream(getMethodParameters(methodDescriptor.getTargetMethod(), methodDescriptor.getContainerClass()))
				.collect(Collectors.toList());

	}

}
