package com.datmt.simple_server;

import io.undertow.Undertow;
import io.undertow.server.handlers.resource.PathResourceManager;
import io.undertow.util.MimeMappings;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.nio.file.Paths;
import java.util.prefs.Preferences;

import static io.undertow.Handlers.resource;

public class Controller {

    @FXML
    TextField portNumberTF;

    @FXML
    BorderPane rootPane;

    @FXML
    Label webRootPath;

    @FXML
    Label errorLog;

    private String webFolder;
    private Undertow server;
    private int port = 18888;


    @FXML
    public void initialize() {
        port = getPrefs().getInt("port", 0);
        webFolder = getPrefs().get("rootDir", null);

        if (webFolder != null) {
            webRootPath.setText(webFolder);
        }

        if (port > 0) {
            portNumberTF.setText(port + "");
        }
    }

    public void selectWebFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(rootPane.getScene().getWindow());

        if (selectedDirectory != null) {
            webFolder = selectedDirectory.getAbsolutePath();
            webRootPath.setText(webFolder);
            saveDir(webFolder);
        }
    }

    private Preferences getPrefs() {
        return Preferences.userNodeForPackage(com.datmt.simple_server.Controller.class);
    }
    private void savePort(int port) {
        getPrefs().putInt("port", port);
    }

    private void saveDir(String dir) {
        getPrefs().put("rootDir", dir);
    }

    public void startWebServer() {
        errorLog.setText("");

        if (server != null) {
            server.stop();
        }

        try {
            int userPort = Integer.parseInt(portNumberTF.getText());

            if (userPort > 0) {
                port = userPort;
                savePort(port);
            }

        } catch (Exception ex) {
            errorLog.setText("Wrong port number");
            return;
        }

        if (webFolder == null) {
            errorLog.setText("Invalid web folder");
            return;
        }

        try {
            server = Undertow.builder()
                    .addHttpListener(port, "0.0.0.0")
                    .setHandler(
                            resource(
                                    new PathResourceManager(Paths.get(webFolder), 100))
                                    .setDirectoryListingEnabled(true)
                                    .setMimeMappings(MimeMappings.builder()
                                            .addMapping("mkv", "video/x-matroska")
                                            .addMapping("mp4", "video/mp4")
                                            .build())
                    )


                    .build();

            server.start();
        } catch (Exception ex) {
            errorLog.setText(ex.getMessage());
        }

    }

}
