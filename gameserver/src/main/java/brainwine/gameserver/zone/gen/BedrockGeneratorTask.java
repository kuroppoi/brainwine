package brainwine.gameserver.zone.gen;

import brainwine.gameserver.item.Layer;

public class BedrockGeneratorTask implements GeneratorTask {

    @Override
    public void generate(GeneratorContext ctx) {
        for(int x = 0; x < ctx.getWidth(); x++) {
            ctx.updateBlock(x, ctx.getHeight() - 1, Layer.FRONT, 666);
        }
    }
}
