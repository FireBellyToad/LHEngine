package com.faust.lhengine.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Shader wrapper class
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public class ShaderWrapper {

    private final ShaderProgram shaderProgram;
    private final Map<String, Object> flags = new HashMap<>();

    /**
     * @param vertexShaderFile
     * @param fragmentShaderFile
     */
    public ShaderWrapper(String vertexShaderFile, String fragmentShaderFile, boolean isWebBuild) {
        Objects.requireNonNull(vertexShaderFile);
        Objects.requireNonNull(fragmentShaderFile);

        final String vertexShader = Gdx.files.internal(vertexShaderFile).readString();
        final String webBuildHeader = Gdx.files.internal("shaders/web_build_header.glsl").readString();
        final String fragmentShader = (isWebBuild ? webBuildHeader : "" )+ Gdx.files.internal(fragmentShaderFile).readString();

        shaderProgram = new ShaderProgram(vertexShader, fragmentShader);
        if (!shaderProgram.isCompiled())
            throw new GdxRuntimeException("Couldn't compile shader: " + shaderProgram.getLog());

    }

    /**
     * Add flag
     */
    public void addFlag(String name, Object value){
        Objects.requireNonNull(name);
        Objects.requireNonNull(value);
        flags.put(name,value);
    }

    /**
     * Initialize shader with set flags
     * @param batch
     */
    public void setShaderOnBatchWithFlags(SpriteBatch batch) {

        shaderProgram.bind();

        flags.forEach((name, value) -> {
            if (value instanceof Boolean) {
                shaderProgram.setUniformi(name, ((Boolean) value) ? 1 : 0);
            } else if (value instanceof Integer) {
                shaderProgram.setUniformi(name, ((Integer) value));
            } else {
                throw new GdxRuntimeException("Invalid flag value for shader " + shaderProgram.getLog());
            }
        });

        batch.setShader(shaderProgram);
    }

    /**
     * resets default shader on batch
     *
     * @param batch
     */
    public void resetDefaultShader(Batch batch) {

        batch.setShader(null);
        shaderProgram.bind();
    }

}
