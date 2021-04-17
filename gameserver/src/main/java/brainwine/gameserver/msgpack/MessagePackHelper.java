package brainwine.gameserver.msgpack;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.zip.DataFormatException;

import org.msgpack.MessagePack;
import org.msgpack.packer.BufferPacker;
import org.msgpack.unpacker.BufferUnpacker;

import brainwine.gameserver.item.Item;
import brainwine.gameserver.msgpack.models.BlockUseData;
import brainwine.gameserver.msgpack.templates.BlockTemplate;
import brainwine.gameserver.msgpack.templates.BlockUseDataTemplate;
import brainwine.gameserver.msgpack.templates.ChunkTemplate;
import brainwine.gameserver.msgpack.templates.EnumTemplate;
import brainwine.gameserver.msgpack.templates.ItemTemplate;
import brainwine.gameserver.reflections.ReflectionsHelper;
import brainwine.gameserver.util.ZipUtils;
import brainwine.gameserver.zone.Block;
import brainwine.gameserver.zone.Chunk;

/**
 * Static instance for the MsgPack library.
 */
public class MessagePackHelper {
    
    private static final MessagePack messagePack = new MessagePack();
    
    static {
        registerTemplates();
    }
    
    private static void registerTemplates() {
        messagePack.register(Item.class, new ItemTemplate());
        messagePack.register(Block.class, new BlockTemplate());
        messagePack.register(Chunk.class, new ChunkTemplate());
        messagePack.register(BlockUseData.class, new BlockUseDataTemplate());
        registerEnumTemplates();
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static void registerEnumTemplates() {
        for(Class<?> clazz : ReflectionsHelper.getTypesAnnotatedWith(RegisterEnum.class)) {
            messagePack.register(clazz, new EnumTemplate(clazz));
        }
    }
    
    public static BufferUnpacker readFile(File file) throws IOException, DataFormatException {
        byte[] bytes = Files.readAllBytes(file.toPath());
        bytes = ZipUtils.inflateBytes(bytes);
        return createBufferUnpacker(bytes);
    }
    
    public static BufferUnpacker readFiles(File... files) throws IOException, DataFormatException, IndexOutOfBoundsException {
        byte[] buffer = new byte[Short.MAX_VALUE];
        int index = 0;
        
        for(File file : files) {
            byte[] bytes = Files.readAllBytes(file.toPath());
            bytes = ZipUtils.inflateBytes(bytes);
            System.arraycopy(bytes, 0, buffer, index, bytes.length);
            index += bytes.length;
        }
        
        byte[] bytes = new byte[index];
        System.arraycopy(buffer, 0, bytes, 0, bytes.length);
        return createBufferUnpacker(bytes);
    }
    
    public static void writeToFile(File file, Object... objects) throws IOException {
        BufferPacker packer = createBufferPacker();
        
        for(Object object : objects) {
            packer.write(object);
        }
        
        byte[] bytes = packer.toByteArray();
        bytes = ZipUtils.deflateBytes(bytes);
        packer.close();
        Files.write(file.toPath(), bytes);
    }
    
    public static BufferUnpacker createBufferUnpacker(byte[] bytes) {
        return messagePack.createBufferUnpacker(bytes);
    }
    
    public static BufferPacker createBufferPacker() {
        return messagePack.createBufferPacker();
    }
}
