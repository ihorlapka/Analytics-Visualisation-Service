package com.iot.devices.management.analytics_visualisation_service.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.function.TriFunction;

import java.util.Optional;

import static java.util.Optional.*;

@UtilityClass
@SuppressWarnings("all")
public class OptionalUtils {

    public static <T, U> Optional<U> ifAllPresentGet(Optional<T> o1, Optional<T> o2, Optional<T> o3, TriFunction<T, T, T, U> function) {
        return (o1.isPresent() && o2.isPresent() && o3.isPresent()) ? ofNullable(function.apply(o1.get(), o2.get(), o3.get())) : empty();
    }
}
