package bnc.testnet.viewer;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.swing.*;
import java.awt.*;

@SpringBootApplication
@EnableScheduling
public class Application implements CommandLineRunner {

    private static ConfigurableApplicationContext context;

    public static void main(String[] args) {
        context = SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) {
        runFrame("", false);
    }

    public static void runFrame(String x, boolean exit) {
        JFrame frame = new JFrame("FrameDemo");
        if (exit) {
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }
        frame.setPreferredSize(new Dimension(500, 500));
        frame.pack();
//5. Show it.
        frame.setVisible(true);

        final JFXPanel jfxPanel = new JFXPanel();
        frame.getContentPane().add(jfxPanel, BorderLayout.CENTER);
//        frame.add(jfxPanel);


        Platform.runLater(new Runnable(){
            @Override
            public void run() {
                WebView webView = new WebView();
                jfxPanel.setScene(new Scene(webView));
//			    webView.getEngine().load("http://localhost:8080/test/" + x);
                webView.getEngine().load("http://localhost:8080/");
            }
        });
    }
}
