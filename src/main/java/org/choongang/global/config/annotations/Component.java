package org.choongang.global.config.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE) // 어디에 적용시킬지, TYPE(클래스, 인터페이스(애너테이션 포함), 열거형)
@Retention(RetentionPolicy.RUNTIME) // 언제까지 유지할지, 런타임
public @interface Component {
}