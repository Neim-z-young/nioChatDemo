package com.oyoungy;

import java.io.IOException;

public class Client1 {
    public static void main(String[] args) throws IOException {
        NioChatClient nioChatClient = new NioChatClient("oyoungy");
        nioChatClient.start();
    }
}
