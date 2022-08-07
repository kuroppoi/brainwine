package brainwine.gameserver.item;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum ItemGroup {
    
    BALLOON,
    BARREL,
    BRICK,
    BUTTERFLY,
    CAGE,
    CHEST,
    COLLECTOR,
    CRYSTAL,
    DOOR,
    ECOLOGY,
    EGG,
    ENEMY_PROTECTOR,
    FILET,
    FUNGI,
    GRAVESTONE,
    GROWABLES,
    HATCHET,
    INFERNAL,
    INHIBITOR,
    JAR,
    JERKY,
    MINERAL,
    PICKAXE,
    PISTOL,
    PLAQUE,
    REMAINS,
    ROOT,
    SACK,
    SALT,
    SHOVEL,
    SHRUB,
    SLEDGEHAMMER,
    SPADE,
    SPELEOTHEM,
    STEAM,
    TREE,
    WALLPAPER,
    
    @JsonEnumDefaultValue
    NONE;
}
