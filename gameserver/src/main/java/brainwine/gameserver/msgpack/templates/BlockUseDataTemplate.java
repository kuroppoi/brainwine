package brainwine.gameserver.msgpack.templates;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.template.AbstractTemplate;
import org.msgpack.type.ValueType;
import org.msgpack.unpacker.Unpacker;

import brainwine.gameserver.msgpack.models.BlockUseData;

public class BlockUseDataTemplate extends AbstractTemplate<BlockUseData> {
	
	@Override
	public void write(Packer packer, BlockUseData data, boolean required) throws IOException {
		if(data == null) {
			if(required) {
				throw new MessageTypeException("Attempted to write null");
			}
			
			packer.writeNil();
			return;
		}
		
		if(data.hasMetadata()) {
			packer.write(data.getMetadata());
		} else if(data.hasPosition()) {
			packer.write(data.getPosition());
		}
	}
	
	@Override
	public BlockUseData read(Unpacker unpacker, BlockUseData to, boolean required) throws IOException {
	    if(!required && unpacker.trySkipNil()) {
	    	return null;
	    }
	    
		if(unpacker.getNextType() == ValueType.ARRAY) {
			int[] position = unpacker.read(int[].class);
			
			if(position.length != 2) {
				throw new MessageTypeException("Invalid array length for position");
			}
			
			return new BlockUseData(position);
		} else if(unpacker.getNextType() == ValueType.MAP) {
			Map<String, Object> metadata = new HashMap<>();
			int numEntries = unpacker.readMapBegin();
			
			for(int i = 0; i < numEntries; i++) {
				String key = unpacker.readString();
				
				switch(unpacker.getNextType()) {
				case RAW:
					metadata.put(key, unpacker.readString());
					break;
				case FLOAT:
					metadata.put(key, unpacker.readFloat());
					break;
				default:
					throw new MessageTypeException("Invalid metadata value type");
				}
			}
			
			unpacker.readMapEnd();
			return new BlockUseData(metadata);
		}
		
		throw new MessageTypeException("Invalid data type");
	}
}
