package com.nightlight.webrtc;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpSignalingClient {

    public String send(String serverUrl, String offerSdp) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(serverUrl);
            connection = (HttpURLConnection) url.openConnection();
            // Configure the connection properties
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept", "application/json, text/plain, */*");
            connection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9");
            connection.setRequestProperty("Cache-Control", "no-cache");
            connection.setRequestProperty("Connection", "keep-alive");
            connection.setRequestProperty("Content-Type", "text/plain;charset=UTF-8");
            connection.setRequestProperty("Pragma", "no-cache");
            connection.setDoOutput(true);

            // Write the post data
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
            try {
                writer.write(offerSdp);  // Send SDP offer data
            } finally {
                writer.close();
            }

            // Read the response
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            try {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();  // Return the response directly
            } finally {
                reader.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;  // Return error message if exception occurs
        } finally {
            if (connection != null) {
                connection.disconnect();  // Properly disconnect the connection
            }
        }
    }
}

