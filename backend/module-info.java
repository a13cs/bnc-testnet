// todo
module bnc.testnet.viewer {
    requires spring.web;
    requires spring.boot.autoconfigure;
    requires spring.boot;
    requires java.desktop;
    requires javafx.web;
    requires javafx.swing;
    requires org.slf4j;
    requires spring.context;
    requires ta4j.core;
    requires commons.compiler;
    requires janino;
    requires spring.beans;
    requires org.apache.tomcat.embed.core;
    requires transitive java.basic;

    exports bnc.testnet.viewer;
}