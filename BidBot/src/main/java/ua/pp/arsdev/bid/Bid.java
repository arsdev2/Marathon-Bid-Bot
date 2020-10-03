package ua.pp.arsdev.bid;

import java.util.Date;

public class Bid {
    private String url, sum;
    private long timestamp;

    public Bid(String _url, String _sum){
        this.url = _url;
        this.sum = _sum;
        this.timestamp = new Date().getTime();
    }

    String getUrl() {
        return url;
    }
    String getSum() {
        return sum;
    }
    long getTimestamp() { return timestamp; }
}
