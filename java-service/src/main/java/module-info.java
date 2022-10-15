module com.example {
    requires spring.web;
    requires spring.boot.autoconfigure;
    requires spring.boot;
    requires gson;
    requires org.apache.tomcat.embed.websocket;
    requires org.apache.commons.lang3;
    requires lombok;
    requires spring.context;
    requires ta4j.core;
    requires com.fasterxml.jackson.annotation;
    exports com.example;
}