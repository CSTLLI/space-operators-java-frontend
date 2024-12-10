module com.example.space_operators_java {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires org.java_websocket;
    requires java.json;

    opens com.example.space_operators_java to javafx.fxml;
    exports com.example.space_operators_java;
    exports com.example.space_operators_java.controllers;
    opens com.example.space_operators_java.controllers to javafx.fxml;
    exports com.example.space_operators_java.utils;
    opens com.example.space_operators_java.utils to javafx.fxml;
    exports com.example.space_operators_java.services;
    opens com.example.space_operators_java.services to javafx.fxml;
}