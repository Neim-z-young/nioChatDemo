package com.oyoungy;

import java.io.IOException;

public class Client2 {
    public static void main(String[] args) throws IOException {
        NioChatClient nioChatClient = new NioChatClient("oygy");
        nioChatClient.start();
    }
}
