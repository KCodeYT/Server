package cn.nukkit.level.particle;

import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.protocol.bedrock.data.LevelEventType;

/**
 * Created on 2015/11/21 by xtypr.
 * Package cn.nukkit.level.particle in project Nukkit .
 */
public class ExplodeParticle extends GenericParticle {
    public ExplodeParticle(Vector3f pos) {
        super(pos, LevelEventType.PARTICLE_EXPLODE);
    }
}
