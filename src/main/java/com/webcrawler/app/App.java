package com.webcrawler.app;


import static com.webcrawler.app.SiteMapPrinter.dummyPrintMap;
import static com.webcrawler.app.WebCrawler.connectionLink;

public class App {


    private static final String ERROR_MESSAGE = "Please enter domain, start page, [max_num_threads], [max depth]. Example:\n" +
            "java -cp target/webcrawler-1.0-SNAPSHOT.jar com.webcrawler.app.App http://www.example.com http://www.example.com/ 5 2";

    private int maxThreads = 5;
    private int maxDepth = 2;

    public App(String[] args) {

        try {
            String domain = args[0];
            String startPage = args[1];
            if (args.length > 2 && args[2] != null) {
                maxThreads = Integer.parseInt(args[2]);
            }

            if (args.length > 3 && args[3] != null) {
                maxDepth = Integer.parseInt(args[3]);
            }

            WebCrawler.startCrawling(domain, startPage, maxThreads, maxDepth);

            System.out.println("\n\n === PRINTING SITE GRAPH ==== \n\n ");
            dummyPrintMap(connectionLink);


        } catch (Exception e) {
            System.out.println("Error: " + e);
            System.out.println(ERROR_MESSAGE);
        }
    }

    public static void main( String[] args )
    {
        new App(args);
    }

}
