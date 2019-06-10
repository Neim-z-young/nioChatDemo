package com.oyoungy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Scanner;

public class NioChatClient {
    private String username;
    NioChatClient(String username){
        this.username = username;
    }
    public void start() throws IOException {
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 8000));
        socketChannel.configureBlocking(false);
        Selector selector = Selector.open();
        socketChannel.register(selector, SelectionKey.OP_READ);
        /**
         * 启用线程处理服务器中发送过来的消息
         */
        Thread thread = new Thread(new NioChatClientHandler(selector));
        thread.start();

        /**
         * 客户端发送的字符经服务器传输后广播至其他客户端的流程为
         * scanner输入->客户端本地转换->编码传入socketChannel->socketChannel中解码->broadcast编码->其他客户端解码->打印至控制台
         * GBK           UTF-8              encode(UTF-8)       decode(UTF-8)        encode(UTF-8) decode(UTF-8)  UTF-8(控制台中显示为GBK)
         */
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()){
            /**
             * 键盘中输入的字符编码是GBK格式的，
             * 而gradle已经设置为编译文件采用UTF-8格式，即文件中的硬编码字符串为UTF-8格式的，
             * 所以要先将字符编码转换为UTF-8后再发送给服务器
             */
            String request = scanner.nextLine();
            if(request != null && request.length()>0){
                socketChannel.write(Charset.forName("UTF-8").encode(
                        username+" : "+new String(request.getBytes("GB18030"), "UTF-8")));
            }
        }
    }

    public static void main(String[] args) throws IOException {
        NioChatClient nioChatClient = new NioChatClient("1 ");
        nioChatClient.start();
    }
}
