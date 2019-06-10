package com.oyoungy;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

/**
 * nio服务器
 */
public class NioChatServer {
    public void start() throws IOException {
        /**
         * 1.创建Selector
         */
        Selector selector = Selector.open();
        /**
         * 2.创建ServerSocketChannel
         */
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        /**
         * 3.channel绑定端口号
         */
        serverSocketChannel.bind(new InetSocketAddress(8000));
        /**
         * 4.设置sockeChannel为阻塞模式
         */
        serverSocketChannel.configureBlocking(false);
        /**
         * 将socketChannel注册到selector上进行监听，并设置为接受模式
         */
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("服务器启动成功!!");

        /**
         * 轮询selector，查找已就绪的selectionKey
         */
        for(;;){
            int readyChannels = selector.select();
            if(readyChannels == 0){
                continue;
            }
            /**
             * 根据就绪状态，调用对应方法处理业务逻辑
             */
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectionKeys.iterator();
            while(keyIterator.hasNext()){
                SelectionKey selectionKey = keyIterator.next();

                if(selectionKey.isAcceptable()){
                    acceptHandler(serverSocketChannel, selector);
                }else if(selectionKey.isReadable()){
                    readableHandler(selectionKey, selector);
                }
                // 由于 select() 操作只是向 Selector 所关联的键集合中添加元素
                // 因此，如果不移除每个处理过的键，
                // 它就会在下次调用 select() 方法时仍然保留在集合中
                // 而且可能会有无用的操作来调用它。
                keyIterator.remove();
            }
        }

    }

    /**
     * 接受事件处理器
     * @param serverSocketChannel
     * @param selector
     * @throws IOException
     */
    public void acceptHandler(ServerSocketChannel serverSocketChannel, Selector selector) throws IOException {
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        /**
         * 将socketChannel注册到selector上
         */
        socketChannel.register(selector, SelectionKey.OP_READ);
        socketChannel.write(Charset.forName("UTF-8").encode("您已成功接入聊天室"));
    }

    /**
     * 可读事件处理器
     * @param selectionKey
     * @param selector
     * @throws IOException
     */
    public void readableHandler(SelectionKey selectionKey, Selector selector) throws IOException {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        String request = "";
        while (socketChannel.read(byteBuffer)>0){
            byteBuffer.flip();
            request += Charset.forName("UTF-8").decode(byteBuffer);
        }
        if(request.length()>0){
            broadcast(selector, socketChannel, request);
        }
    }

    /**
     * 将用户消息广播到所有已连接的用户
     * @param selector
     * @param sourceChannel
     * @param request
     */
    public void broadcast(Selector selector, SocketChannel sourceChannel ,String request){
        Set<SelectionKey> selectionKeys= selector.keys();
        selectionKeys.forEach(selectionKey -> {
            Channel channel = selectionKey.channel();
            if(channel instanceof SocketChannel && channel!=sourceChannel){
                try {
                    System.out.println(request);
                    ((SocketChannel) channel).write(Charset.forName("UTF-8").encode(request));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void main(String[] args) throws IOException {
        NioChatServer nioChatServer = new NioChatServer();
        nioChatServer.start();
    }

}
