package com.strauteka.example.entity;

import lombok.SneakyThrows;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.beans.FeatureDescriptor;
import java.util.stream.Stream;

public class EntityUtils {
    @SneakyThrows
    public static <T> T combine2Objects(T base, T override) {
        @SuppressWarnings("unchecked")
        T result = (T) base.getClass().getDeclaredConstructor().newInstance();
        BeanUtils.copyProperties(base, result);
        BeanUtils.copyProperties(override, result, getNullPropertyNames(override));
        return result;
    }

    //copy from www \m/(o.o)\m/
    private static String[] getNullPropertyNames(Object source) {
        final BeanWrapper wrappedSource = new BeanWrapperImpl(source);
        return Stream.of(wrappedSource.getPropertyDescriptors())
                .map(FeatureDescriptor::getName)
                .filter(propertyName -> wrappedSource.getPropertyValue(propertyName) == null)
                .toArray(String[]::new);
    }
}
