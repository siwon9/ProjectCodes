package org.choongang.global.config.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD}) // 메서드도 적용가능
@Retention(RetentionPolicy.RUNTIME)
public @interface DeleteMapping {
    String[] value();
}