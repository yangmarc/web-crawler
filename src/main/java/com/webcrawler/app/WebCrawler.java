package com.webcrawler.app;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class WebCrawler {

    protected static final Map<String,Integer> allUrlDepth = new HashMap<>();
    protected static final Map<String, List<String>> connectionLink = new HashMap<>();

    private static final List<String> allWaitUrl = new ArrayList<>();
    private static final Set<String> allOverUrl = new HashSet<>();
    private static final Object objectLock = new Object();
    private static int availableThreads = 0;

    private static int MAX_THREAD;
    private static int MAX_DEPTH;
    private static String domainRestriction;

    public static void startCrawling(String domain, String url, int maxThreads, int maxDepth){

        domainRestriction = domain;

        MAX_THREAD = maxThreads;
        MAX_DEPTH = maxDepth;

        addurl(null, url,0);

        ExecutorService es = Executors.newCachedThreadPool();

        for(int i = 0 ; i < MAX_THREAD; i++) {
            es.execute(new WebCrawlerThread());
        }
        es.shutdown();

        // wait until all threads finish their work
        while(!es.isTerminated());

    }

    public static void crawlURL(String startUrl, int depth){

        if(!(allOverUrl.contains(startUrl)||depth > MAX_DEPTH)){

            System.out.println("PROCESSING THREAD："+Thread.currentThread().getName() + " PROCESSING URL："+startUrl);

            try {
                URL url = new URL(startUrl);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                int code = conn.getResponseCode();

                // wait and retry if its 429
                if (code == 429) {
                    timeToRetryAfter();
                    crawlURL(startUrl, depth);
                }

                InputStream is = conn.getInputStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(is,"GB2312"));

                String line;
                Pattern p=Pattern.compile("<a .*href=.+</a>");

                while((line=br.readLine())!=null){
                    Matcher m=p.matcher(line);
                    while(m.find()){
                        String href = m.group();

                        href=href.substring(href.indexOf("href="));
                        if(href.charAt(5)=='\"'){
                            href=href.substring(6);
                        }else{
                            href=href.substring(5);
                        }

                        try{
                            href = href.substring(0,href.indexOf("\""));
                        }catch(Exception e){
                            try{
                                href = href.substring(0,href.indexOf(" "));
                            }catch(Exception e1){
                                href = href.substring(0,href.indexOf(">"));
                            }
                        }
                        if(href.contains(domainRestriction) && (href.startsWith("http:")||href.startsWith("https:"))){
                            addurl(startUrl, href,depth);
                        } else if (!href.isEmpty() && href.charAt(0) == '/') {

                            href = domainRestriction + href;
                            addurl(startUrl, href,depth);
                        }

                    }

                }

                br.close();
            } catch (Exception e) {

                e.printStackTrace();
            }

            allOverUrl.add(startUrl);
            System.out.println("DONE EXECUTING URL: " + startUrl + " ALREADY PROCESSED："+ allOverUrl.size()+", NUMBER of URLS LEFT："+ allWaitUrl.size());
        }

        if(allWaitUrl.size()>0){
            synchronized(objectLock){
                objectLock.notify();
            }
        }else{
            System.out.println("THREAD : " + Thread.currentThread().getName() + " IS DONE CRAWLING......");
        }

    }

    private static void timeToRetryAfter() throws InterruptedException {
        Thread.sleep(10000);
    }


    public static synchronized void addurl(String parentHref, String href,int depth){

        if(!allUrlDepth.containsKey(href)){
            allWaitUrl.add(href);
            allUrlDepth.put(href, depth+1);
            connectionLink.put(href, new ArrayList<String>());
        }

        if (parentHref != null && connectionLink.get(parentHref) != null) {
            connectionLink.get(parentHref).add(href);
        }

    }

    public static synchronized String geturl(){
        if (allWaitUrl.size() <= 0) {
            return null;
        }

        String nexturl= allWaitUrl.get(0);
        allWaitUrl.remove(0);
        return nexturl;
    }

    public static class WebCrawlerThread extends Thread {
        @Override
        public void run(){

            while(true){

                if(allWaitUrl.size()>0){

                    String url=geturl();
                    if (url != null) {

                        crawlURL(url, allUrlDepth.get(url));
                    }
                }

                if (allWaitUrl.size() <= 0) {
                    System.out.println("CURRENT THREAD IS WAITING, READY TO CRAWL："+this.getName());

                    availableThreads++;
                    if (availableThreads >= MAX_THREAD) {
                        break;
                    }

                    synchronized(objectLock){
                        try{
                            objectLock.wait(20000);
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                    if (allWaitUrl.size() > 0) {
                        availableThreads--;
                    }

                }

            }
        }

    }
}