package brainwine.gameserver.entity;

import brainwine.gameserver.msgpack.RegisterEnum;

@RegisterEnum
public enum EntityStatus {
    
    EXITING,
    ENTERING,
    DEAD,
    REVIVED;
}
