package com.jewellery;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

// Main class: this is the class you RUN in IntelliJ IDEA (right-click -> Run 'Main').
// It starts a small web server (Apache Tomcat, embedded - its jar files are in the "lib" folder),
// loads our website from the "webapp" folder and opens http://localhost:8080/ in the browser.
// No Maven or separate server installation is needed.
//
// REFERENCE: starting Tomcat from a main() method follows the "Embedded Tomcat" way described in the
// official Apache Tomcat documentation (https://tomcat.apache.org/tomcat-9.0-doc/). The code below was
// written for this project.
public class Main {

    // The port number of the website: http://localhost:8080/
    // A hosting service (Render, Railway ...) tells us its port in the PORT variable, so we read it first.
    private static final int PORT = findPort();

    // Reads the PORT environment variable (if the host gave one), otherwise uses 8080
    private static int findPort() {
        String fromHost = System.getenv("PORT");
        if (fromHost != null) {
            try {
                return Integer.parseInt(fromHost.trim());
            } catch (NumberFormatException e) {
                System.out.println("PORT variable is not a number, using 8080.");
            }
        }
        return 8080;
    }

    public static void main(String[] args) {
        // The folder with the JSP pages, CSS, images and WEB-INF/web.xml
        File webFolder = new File("webapp");
        if (!webFolder.exists()) {
            System.out.println("ERROR: the 'webapp' folder was not found in: " + new File(".").getAbsolutePath());
            System.out.println("In IntelliJ open Run -> Edit Configurations and set 'Working directory' to the project folder.");
            return;
        }

        try {
            Tomcat server = new Tomcat();
            // Tomcat keeps its temporary work files (compiled JSP pages) in the computer's temp folder.
            // We delete the old ones at every start, so an old copy of a page can never be shown
            // after the project was updated.
            File work = new File(System.getProperty("java.io.tmpdir"), "jewellery-tomcat");
            deleteFolder(work);
            server.setBaseDir(work.getAbsolutePath());
            server.setPort(PORT);
            server.getConnector();   // creates the HTTP connector for the port

            // "" means the website opens at the main address (http://localhost:8080/)
            Context website = server.addWebapp("", webFolder.getAbsolutePath());
            website.setParentClassLoader(Main.class.getClassLoader());   // lets the website find our servlet classes

            server.start();
            System.out.println("==============================================");
            System.out.println(" Gemora Jewellery Management System is running");
            System.out.println(" Open:  http://localhost:" + PORT + "/");
            System.out.println(" Password: the admin password (default: admin123)");
            System.out.println(" Stop the server with the red Stop button.");
            System.out.println("==============================================");

            openBrowser("http://localhost:" + PORT + "/");
            server.getServer().await();   // keeps the program running until it is stopped
        } catch (LifecycleException e) {
            // most common reason: the port is already used by another program
            System.out.println("Could not start the server: " + e.getMessage());
            System.out.println("Is another copy of this program (or another program) already using port " + PORT + "?");
        }
    }

    // Deletes a folder with everything inside it (used to clean Tomcat's temporary work folder)
    private static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteFolder(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        folder.delete();
    }

    // Tries to open the website in the default browser. If this is not possible, we only print the address.
    private static void openBrowser(String address) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(address));
            }
        } catch (Exception e) {
            System.out.println("Please open " + address + " in your browser.");
        }
    }
}
