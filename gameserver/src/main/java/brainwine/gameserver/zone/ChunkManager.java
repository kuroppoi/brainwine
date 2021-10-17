package brainwine.gameserver.zone;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.msgpack.unpacker.Unpacker;

import brainwine.gameserver.msgpack.MessagePackHelper;
import brainwine.gameserver.util.ZipUtils;

public class ChunkManager {
	
	private static final Logger logger = LogManager.getLogger();
	private static final String headerString = "brainwine blocks file";
	private static final int latestVersion = 1;
	private static final int dataOffset = headerString.length() + 4;
    private static final int allocSize = 2048;
	private static boolean conversionNotified;
	private final Zone zone;
	private RandomAccessFile file;
	
	public ChunkManager(Zone zone) {
		this.zone = zone;
		
		try {
			if(file == null) {
				File blocksFile = new File(zone.getDirectory(), "blocks.dat");
				File legacyBlocksFile = new File(zone.getDirectory(), "blocks");
				
				if(!blocksFile.exists()) {
					blocksFile.createNewFile();
				}
				
				file = new RandomAccessFile(blocksFile, "rw");
				
				if(file.length() == 0) {
					file.writeUTF(headerString);
					file.writeInt(latestVersion);
					
					if(legacyBlocksFile.exists()) {
						if(!conversionNotified) {
							logger.info("One or more block data files need to be converted. This might take a while ...");
							conversionNotified = true;
						}
						
						convertLegacyBlocksFile(legacyBlocksFile);
					}
				} else {
					if(!file.readUTF().equals(headerString)) {
						throw new IOException("Invalid header string");
					}
				}
			}
		} catch(Exception e) {
			logger.error("ChunkManager construction for zone {} failed", zone.getDocumentId(), e);
		}
	}
	
	private void convertLegacyBlocksFile(File legacyBlocksFile) throws Exception {
		byte[] bytes = Files.readAllBytes(legacyBlocksFile.toPath());
		
		for(int i = 0; i < bytes.length; i += 2048) {
			short length = (short)(((bytes[i] & 0xFF) << 8) + (bytes[i + 1] & 0xFF));
			byte[] chunkBytes = ZipUtils.inflateBytes(Arrays.copyOfRange(bytes, i + 2, i + 2 + length));
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			Unpacker unpacker = MessagePackHelper.createBufferUnpacker(chunkBytes);
			unpacker.readArrayBegin();
			int x = unpacker.readInt();
			int y = unpacker.readInt();
			int width = unpacker.readInt();
			int height = unpacker.readInt();
			dos.writeInt(x);
			dos.writeInt(y);
			dos.writeInt(width);
			dos.writeInt(height);
			unpacker.readArrayBegin();
			
			for(int j = 0; j < width * height; j++) {
				dos.writeInt(unpacker.readInt());
				dos.writeInt(unpacker.readInt());
				dos.writeInt(unpacker.readInt());
			}
			
			unpacker.close();
			byte[] updatedBytes = ZipUtils.deflateBytes(baos.toByteArray());
			dos.close();
			file.seek(dataOffset + zone.getChunkIndex(x, y) * allocSize);
			file.writeShort(updatedBytes.length);
			file.write(updatedBytes);
		}
	}
	
	public Chunk loadChunk(int index) {
		Chunk chunk = null;
		DataInputStream dis = null;
		
		try {
			file.seek(dataOffset + index * allocSize);
			byte[] bytes = new byte[file.readShort()];
			file.read(bytes);
			
			dis = new DataInputStream(new ByteArrayInputStream(ZipUtils.inflateBytes(bytes)));
			chunk = new Chunk(dis.readInt(), dis.readInt(), dis.readInt(), dis.readInt());
			
			for(int i = 0; i < zone.getChunkWidth() * zone.getChunkHeight(); i++) {
				chunk.setBlock(i, new Block(dis.readInt(), dis.readInt(), dis.readInt()));
			}
		} catch(Exception e) {
			logger.error("Could not load chunk {} of zone {}", index, zone.getDocumentId(), e);
		} finally {
			if(dis != null) {
				try {
					dis.close();
				} catch (IOException e) {
					logger.warn("Resource could not be closed", e);
				}
			}
		}
		
		return chunk;
	}
	
	public void saveModifiedChunks() {
		for(Chunk chunk : zone.getChunks()) {
			if(chunk.isModified()) {
				saveChunk(chunk);
			}
		}
	}
	
	public void saveChunk(Chunk chunk) {
		DataOutputStream dos = null;
		int index = zone.getChunkIndex(chunk.getX(), chunk.getY());
		
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream(allocSize);
			dos = new DataOutputStream(baos);
			dos.writeInt(chunk.getX());
			dos.writeInt(chunk.getY());
			dos.writeInt(chunk.getWidth());
			dos.writeInt(chunk.getHeight());
			
			for(Block block : chunk.getBlocks()) {
				dos.writeInt(block.getBase());
				dos.writeInt(block.getBack());
				dos.writeInt(block.getFront());
			}
			
			byte[] bytes = ZipUtils.deflateBytes(baos.toByteArray());
			file.seek(dataOffset + index * allocSize);
			file.writeShort(bytes.length);
			file.write(bytes);
			chunk.setModified(false);
		} catch(Exception e) {
			logger.error("Could not save chunk %s of zone %s", index, zone.getDocumentId(), e);
		} finally {
			if(dos != null) {
				try {
					dos.close();
				} catch (IOException e) {
					logger.warn("Resource could not be closed", e);
				}
			}
		}
	}
}
