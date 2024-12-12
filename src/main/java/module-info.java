module com.example.space_operators_java {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires spring.websocket;
    requires spring.messaging;
    requires spring.core;

    requires com.fasterxml.jackson.databind;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.json;
    requires spring.context;
    requires java.net.http;
    requires java.compiler;

    opens com.example.space_operators_java to javafx.fxml;
    exports com.example.space_operators_java;
    exports com.example.space_operators_java.controllers;
    opens com.example.space_operators_java.controllers to javafx.fxml;
    exports com.example.space_operators_java.utils;
    opens com.example.space_operators_java.utils to javafx.fxml;
    exports com.example.space_operators_java.services;
    opens com.example.space_operators_java.services to javafx.fxml;
    exports com.example.space_operators_java.models to com.fasterxml.jackson.databind;
    opens com.example.space_operators_java.models to com.fasterxml.jackson.databind;
    exports com.example.space_operators_java.models.request to com.fasterxml.jackson.databind;
    opens com.example.space_operators_java.models.request to com.fasterxml.jackson.databind;
    exports com.example.space_operators_java.models.response to com.fasterxml.jackson.databind;
    opens com.example.space_operators_java.models.response to com.fasterxml.jackson.databind;
    exports com.example.space_operators_java.models.operation to com.fasterxml.jackson.databind;
    opens com.example.space_operators_java.models.operation to com.fasterxml.jackson.databind;
}