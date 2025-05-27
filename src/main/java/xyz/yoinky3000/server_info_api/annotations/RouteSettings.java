package xyz.yoinky3000.server_info_api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RouteSettings {
    String path();
    boolean requireMinecraftServer() default false;
}
