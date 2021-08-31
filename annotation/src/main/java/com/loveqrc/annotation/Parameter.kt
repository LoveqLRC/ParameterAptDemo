package com.loveqrc.annotation


@Target(AnnotationTarget.FIELD) // 该注解作用在属性之上
@kotlin.annotation.Retention(AnnotationRetention.BINARY)
annotation class Parameter(
    // 不填写name的注解值表示该属性名就是key，填写了就用注解值作为key
    val name: String = ""
)