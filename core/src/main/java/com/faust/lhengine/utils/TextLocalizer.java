package com.faust.lhengine.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Localize a string
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public class TextLocalizer {

    private final Map<String, JsonValue> messageMap = new HashMap<>();
    private String language = "eng";

    /**
     * Load all game text
     *
     */
    public void loadTextFromLanguage() {

        // Prepare text map
        JsonValue root = new JsonReader().parse(Gdx.files.internal("messages/messages_" + this.language + ".json"));
        JsonValue messages =   root.get("messages");

        Objects.requireNonNull(messages);

        messageMap.put("cutscenes",messages.get("cutscenes"));
        messageMap.put("boxes",messages.get("boxes"));
        messageMap.put("menu",messages.get("menu"));

    }

    /**
     * Localize message to current language
     * @param message
     * @param textKey
     * @return
     */
    public String localizeFromKey(String message, String textKey) {
        Objects.requireNonNull(textKey);
        if(messageMap.isEmpty()){
            throw new GdxRuntimeException("messageMap is empty: messages file has not been loaded");
        }

        return messageMap.get(message).getString(textKey);
    }

    public void setLanguage(String language) {
        this.language = language;
    }

}
