package org.example;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CrawlerMain {
    private static String datalakePath = "/Users/luisguillen/Desktop/A_implementar/GreenLanterns-master/Query-Engine";

    public CrawlerMain() {
    }

    public static void main(String[] args) {
        Crawler crawler = new Crawler(datalakePath);
        System.out.println("Starting book downloads...\n");
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            crawler.crawlerRunner();
        }, 1L, 10L, TimeUnit.SECONDS);
    }
}
