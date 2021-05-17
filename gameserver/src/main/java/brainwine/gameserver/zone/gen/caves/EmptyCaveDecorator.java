package brainwine.gameserver.zone.gen.caves;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import brainwine.gameserver.zone.gen.CaveDecorator;
import brainwine.gameserver.zone.gen.GeneratorContext;
import brainwine.gameserver.zone.gen.models.Cave;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EmptyCaveDecorator extends CaveDecorator {

    @Override
    public void decorate(GeneratorContext ctx, Cave cave) {}
}
