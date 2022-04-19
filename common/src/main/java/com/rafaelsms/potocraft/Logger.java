package com.rafaelsms.potocraft;

import org.jetbrains.annotations.NotNull;

public interface Logger {

    @NotNull org.slf4j.Logger logger();

    default void error(String msg) {
        logger().error(msg);
    }

    default void error(String format, Object... arguments) {
        logger().error(format, arguments);
    }

    default void warn(String msg) {
        logger().warn(msg);
    }

    default void warn(String format, Object... arguments) {
        logger().warn(format, arguments);
    }

    default void info(String msg) {
        logger().info(msg);
    }

    default void info(String format, Object... arguments) {
        logger().info(format, arguments);
    }

}
