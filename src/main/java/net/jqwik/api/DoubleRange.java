package net.jqwik.api;

import java.lang.annotation.*;

@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DoubleRange {
	double min() default 0.0;
	double max();
}
