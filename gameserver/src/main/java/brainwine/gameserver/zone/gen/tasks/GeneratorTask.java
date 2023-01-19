package brainwine.gameserver.zone.gen.tasks;

import brainwine.gameserver.zone.gen.GeneratorContext;

public interface GeneratorTask {
    
    public void generate(GeneratorContext ctx);
}
