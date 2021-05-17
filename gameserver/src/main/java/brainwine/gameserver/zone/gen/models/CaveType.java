package brainwine.gameserver.zone.gen.models;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

import brainwine.gameserver.zone.gen.CaveDecorator;
import brainwine.gameserver.zone.gen.caves.BatCaveDecorator;
import brainwine.gameserver.zone.gen.caves.CandyCaneCaveDecorator;
import brainwine.gameserver.zone.gen.caves.CrystalCaveDecorator;
import brainwine.gameserver.zone.gen.caves.EmptyCaveDecorator;
import brainwine.gameserver.zone.gen.caves.MushroomCaveDecorator;
import brainwine.gameserver.zone.gen.caves.RareCrystalCaveDecorator;
import brainwine.gameserver.zone.gen.caves.SaltCaveDecorator;

public enum CaveType {

    @JsonEnumDefaultValue
    EMPTY,
    MUSHROOM_GROVE(MushroomCaveDecorator.class),
    CRYSTAL(CrystalCaveDecorator.class),
    RARE_CRYSTAL(RareCrystalCaveDecorator.class),
    SALT(SaltCaveDecorator.class),
    BAT_CAVE(BatCaveDecorator.class),
    CANDY_CANE(CandyCaneCaveDecorator.class);
    
    private final Class<? extends CaveDecorator> decoratorType;
    
    private CaveType() {
        this(EmptyCaveDecorator.class);
    }
    
    private CaveType(Class<? extends CaveDecorator> decoratorType) {
        this.decoratorType = decoratorType;
    }
    
    public Class<? extends CaveDecorator> getDecoratorType() {
        return decoratorType;
    }
}
