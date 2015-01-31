package com.iskrembilen.quasseldroid.protocol.state.serializers;

import com.iskrembilen.quasseldroid.protocol.qtcomm.QVariantType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})

public @interface Syncable {
    public String name() default "";
    public QVariantType type() default QVariantType.UserType;
    public String userType() default "";
    public QVariantType[] paramTypes() default {};
}
