package ua.pp.arsdev.bid;


import com.google.common.io.Files;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.ActionType;
import org.telegram.telegrambots.api.methods.send.SendChatAction;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;


public class BidBot extends TelegramLongPollingBot {

    public static String BOT_TOKEN, BOT_NAME, TOKEN, FIREBASE_TOKEN = "AIzaSyDRiPOFtGCwU0tAoL3xTPerOlK7J7invYA";
    volatile Bid bid = null;
    private String sum = "";
    private ArrayList<Long> chats = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        ApiContextInitializer.init();
        Properties properties = new Properties();
        properties.load(new FileInputStream("config.properties"));
        BOT_TOKEN = properties.getProperty("bot_token");
        BOT_NAME = properties.getProperty("bot_username");
        TOKEN = String.valueOf((int)(10000 + Math.random()*89999));
        System.out.println(TOKEN);
        Files.write(TOKEN, new File("token.txt"), Charset.forName("UTF-8"));
        BidBot bot = new BidBot();
        TelegramBotsApi botapi = new TelegramBotsApi();
        botapi.registerBot(bot);
        Timer timer = new Timer();
        timer.schedule(new Reload(), 0, 3600 * 1000);

        /*
            DEBUG ONLY!

         */
        bot.chats.add(265084600L);
        bot.sum = "144";
    }
    // And From your main() method or any other method

    @Override
    public void onUpdateReceived(Update update) {
        if(update.hasMessage() && update.getMessage().hasText()){
            Message msg = update.getMessage();
            String msgText = msg.getText();
            if(msgText.contains("/auth")){
                String token = msgText.split(" ")[1];
                if(token.equals(TOKEN)){
                    sendMsg(msg, "Успешная авторизация!");
                    System.out.println(msg.getChatId());
                    chats.add(msg.getChatId());
                }
            }else {
                if(chats.contains(msg.getChatId())) {
                    if (msgText.contains("/changeSum")) {
                        sum = msgText.split(" ")[1];
                        sendMsg(msg, "Сума изменена!");
                    } else {
                        if (!sum.equals("")) {
                            bid = new Bid(msgText, sum);
                            try {
                                JSONObject object = new JSONObject();
                                object.put("type", "bid");
                                object.put("sum", bid.getSum());
                                object.put("url", bid.getUrl());
                                String tokenHash = md5(TOKEN), data = object.toString(), encrypted = encrypt(data, tokenHash);
                                System.out.println(encrypted);
                                sendBid(encrypted);
                            }catch (Exception e){
                                System.out.println("Error sending bid");
                                e.printStackTrace();
                            }
                        } else {
                            sendMsg(msg, "Не указана сума!");
                        }
                    }
                }else{
                    sendMsg(msg, "Авторизируйтесь");
                }
            }
        }
    }

    static void sendBid(String encrypted) throws Exception{

        JSONObject requestJsonObject = new JSONObject(), dataJsonObject = new JSONObject();
        dataJsonObject.put("data", encrypted);
        requestJsonObject.put("to", "/topics/all");
        requestJsonObject.put("data", dataJsonObject);
        requestJsonObject.put("click_action", "main");

        HttpClient client = new DefaultHttpClient();
        StringEntity requestEntity = new StringEntity(
                requestJsonObject.toString(),
                "application/json",
                "UTF-8");
        HttpPost post = new HttpPost("https://fcm.googleapis.com/fcm/send");
        post.setEntity(requestEntity);
        post.setHeader("Authorization", "key=" + FIREBASE_TOKEN);

        HttpResponse response = client.execute(post);
    }

    private static String md5(String st) {
        MessageDigest messageDigest = null;
        byte[] digest = new byte[0];

        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(st.getBytes());
            digest = messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        BigInteger bigInt = new BigInteger(1, digest);
        String md5Hex = bigInt.toString(16);

        while( md5Hex.length() < 32 ){
            md5Hex = "0" + md5Hex;
        }

        return md5Hex;
    }

    public SendMessage sendMsg (Message message, String text){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText(text);
        sendMessage.setParseMode("Markdown");
        SendChatAction action = new SendChatAction();
        action.setChatId(message.getChatId());
        action.setAction(ActionType.TYPING);
        try {
            execute(action);
            execute(sendMessage);
        }catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return sendMessage;
    }

    public static String encrypt(String data, String password) throws Exception{
        SecretKeySpec key = generateKey(password);
        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.ENCRYPT_MODE, key);
        byte[] encVal = c.doFinal(data.getBytes());
        return Base64.encodeBase64String(encVal);
 //       return Base64.getEncoder().encodeToString(encVal, Base64.DEFAULT);
    }

    public static SecretKeySpec generateKey(String password) throws Exception {
        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = password.getBytes("UTF-8");
        digest.update(bytes, 0, bytes.length);
        byte[] key = digest.digest();
        return new SecretKeySpec(key, "AES");
    }

    @Override
    public String getBotUsername() {
        return BOT_NAME;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }
}
class Reload extends TimerTask {
    public void run() {
        try {
            JSONObject object = new JSONObject();
            object.put("type", "refresh");
            BidBot.sendBid(BidBot.encrypt(object.toString(), BidBot.TOKEN));
        }catch (Exception ignored){

        }

    }
}
