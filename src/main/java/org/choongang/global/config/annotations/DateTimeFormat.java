package org.choongang.global.config.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.TYPE_USE}) // 여긴 타겟이 두개, FIELD/멤버변수포함 , 타입사용시 적용
@Retention(RetentionPolicy.RUNTIME)
public @interface DateTimeFormat {
    String value();
}