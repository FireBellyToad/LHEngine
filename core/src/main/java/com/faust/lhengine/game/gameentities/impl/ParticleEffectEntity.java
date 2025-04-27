package com.faust.lhengine.game.gameentities.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.faust.lhengine.game.gameentities.GameEntity;

/**
 * Class that extends GameEntity and adds a particle effectg
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public class ParticleEffectEntity extends GameEntity {

    private final ParticleEffect particleEffect;

    /**
     *
     * @param particleName
     */
    public ParticleEffectEntity(String particleName) {
        super();
        this.particleEffect = new ParticleEffect();
        this.particleEffect.load(Gdx.files.internal("particles/"+particleName), Gdx.files.internal("sprites/"));
    }

    public ParticleEffect getParticleEffect() {
        return particleEffect;
    }
}