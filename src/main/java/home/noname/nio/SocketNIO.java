package home.noname.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class SocketNIO {
    public static void main(String[] args) throws IOException {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        Selector selector = Selector.open();
        ssc.bind(new InetSocketAddress(3000));
        ssc.configureBlocking(false);
        ssc.register(selector, SelectionKey.OP_ACCEPT);
        ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
        while (true) {
            final int select = selector.select();
            if (select == 0)
                continue;
            final Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                final SelectionKey key = iterator.next();
                try {
                    if (key.channel() == ssc) {
                        final SocketChannel channel = ssc.accept();
                        channel.configureBlocking(false);
                        channel.register(selector, SelectionKey.OP_READ);
                    } else {
                        ((SocketChannel) key.channel()).read(buffer);
                        buffer.flip();
                        System.out.println("String: " + new String(buffer.array(), buffer.position(), buffer.remaining()));
                        buffer.clear();
                    }
                } finally {
                    iterator.remove();
                }
            }
        }
    }
}
