package br.com.xmacedo;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import static br.com.xmacedo.Constants.PATH_FILE;

public class MemoryMappedIOExample {
    public static void main(String[] args) throws IOException {
        Path path = Path.of(PATH_FILE);
        try (FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.READ)) {
            long fileSize = fileChannel.size();
            MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileSize);


        }
    }
}
