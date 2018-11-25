package home.noname.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static java.nio.file.StandardOpenOption.*;

public class FileNIO {
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        ByteBuffer buffer = ByteBuffer.allocate(10);
        AsynchronousFileChannel input = AsynchronousFileChannel.open(Paths.get("file.txt"), READ);
        AsynchronousFileChannel output = AsynchronousFileChannel.open(Paths.get("file2.txt"), WRITE, CREATE, TRUNCATE_EXISTING);
        CompletableFuture<Integer> c = fcopy(input, output, 0, buffer);
        CompletableFuture<Object> thenApply = c.thenApply(s -> {
            System.out.println("continue");
            return null;
        });
        thenApply.get();
    }




    static CompletableFuture<Integer> fread(AsynchronousFileChannel input, int position, ByteBuffer buffer) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        input.read(buffer, position, buffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                buffer.flip();
                future.complete(result);
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                future.completeExceptionally(exc);
            }
        });
        return future;
    }

    static CompletableFuture<Integer> fwrite(AsynchronousFileChannel output, int position, ByteBuffer buffer) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        output.write(buffer, position, buffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                buffer.compact();
                future.complete(result);
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                future.completeExceptionally(exc);
            }
        });
        return future;
    }

    static CompletableFuture<Integer> fcopy(AsynchronousFileChannel input, AsynchronousFileChannel output, int from, ByteBuffer buffer) {
        return fread(input, from, buffer).thenCompose(r -> r != -1 ? fwrite(output, from, buffer).thenCompose(r0 -> fcopy(input, output, from + r0, buffer)): CompletableFuture.completedFuture(0));
    }
}
