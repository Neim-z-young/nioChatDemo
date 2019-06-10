package com.oyoungy;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

public class NioChatClientHandler implements Runnable {

    private Selector selector;

    NioChatClientHandler(Selector selector){
        this.selector = selector;
    }

    @Override
    public void run() {
        for(;;){
            try {
                int readyChannels = selector.select();
                if (readyChannels == 0) {
                    continue;
                }
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectionKeys.iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey selectionKey = keyIterator.next();
                    if (selectionKey.isReadable()) {
                        System.out.println("有可读消息");
                        readableHandler(selectionKey, selector);
                    }
                    keyIterator.remove();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void readableHandler(SelectionKey selectionKey, Selector selector) throws IOException {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        String response = "";
        while (socketChannel.read(byteBuffer)>0){
            byteBuffer.flip();
            response += Charset.forName("UTF-8").decode(byteBuffer);
        }
        if(response.length()>0){
            System.out.println(response);
        }
    }
}
