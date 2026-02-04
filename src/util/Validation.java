package util;

import exception.ValidationException;

@FunctionalInterface
public interface Validation<T> {
    void validate(T value) throws ValidationException;
}
