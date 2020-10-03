package com.arsdev.bidapplication;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        ArrayList<Thread> threads = new ArrayList<>();
        for(int i = 0;i<20;i++){
            Thread t = new Thread(new Runnable() {
                @Override
                public void run(){
                    try {
                        URL obj = new URL("http://104.248.39.214/api/getBid?code=33565");
                        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                        BufferedReader in = new BufferedReader(
                                new InputStreamReader(con.getInputStream()));
                        String inputLine;
                        StringBuilder response = new StringBuilder();
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        in.close();
                        String resp = response.toString();
                        System.out.println(resp);
                    }catch (ConnectException e){

                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
            t.start();
            threads.add(t);
        }
        for(Thread t : threads){
            t.join();
        }
    }
}