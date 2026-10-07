package lcsfw.fw.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import lcsfw.fw.http.HttpMethode;

// Associe une methode de controleur a une URL et a un verbe HTTP.
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface UrlMapping {
    String url();
    HttpMethode method();
}
