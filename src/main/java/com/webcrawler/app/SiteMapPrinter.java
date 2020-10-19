package com.webcrawler.app;

import java.util.*;

public class SiteMapPrinter {


    /**
     *
     * Dummy print all the URLs with relations to other URLs
     */
    public static void dummyPrintMap(Map<String, List<String>> connectionLink) {

        for (String url : connectionLink.keySet()) {
            if (connectionLink.get(url).size() > 0) {
                System.out.print(url);
                System.out.println(" |------> ");
                System.out.println(connectionLink.get(url));
                System.out.println("\n\n");
            }
        }
    }

}
