package com.faust.lhengine.game.scripts.enums;


/**
 * Script Actor entity class
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public enum ScriptActorType {
    DISCIPULUS("echo.discipulus.json","sprites/discipulus_sheet.png", "sounds/terror.ogg"),
    VICTIM("echo.victim.json","sprites/victim_sheet.png"),
    WOMAN("echo.woman.json","sprites/woman_sheet.png","sounds/horror_scream.ogg"),
    DEAD_HAND("echo.hand.json","sprites/hand_sheet.png"),
    DEAD_DOUBLE_HAND("echo.hands.json","sprites/double_hand_sheet.png", "sounds/rattling-bones.ogg"),
    HORROR("echo.horror.json","sprites/horror_sheet.png"),
    HORROR_BODY("echo.horrorbody.json","sprites/horror_body_sheet.png", "sounds/terror.ogg"),
    INFERNUM("echo.infernum.json","sprites/fleshpillar_sheet.png", "sounds/terror.ogg"),
    FLESH_PILLAR("echo.fleshpillar.json","sprites/fleshpillar_sheet.png"),
    PORTAL_SPAWNER("echo.portal.json"),
    SECRET("echo.secret.json"),
    DIACONUS("echo.diaconus.json","sprites/diaconus_echo_sheet.png", "sounds/terror.ogg");

    private final String filename;
    private final String spriteFilename;
    private final String soundFileName;

    ScriptActorType(String filename) {
        this.filename = filename;
        this.spriteFilename = "sprites/fleshpillar_sheet.png";
        this.soundFileName = null;
    }

    ScriptActorType(String filename, String spriteFilename) {
        this.filename = filename;
        this.spriteFilename = spriteFilename;
        this.soundFileName = null;
    }

    ScriptActorType(String filename, String spriteFilename, String soundFileName) {
        this.filename = filename;
        this.spriteFilename = spriteFilename;
        this.soundFileName = soundFileName;
    }

    public String getFilename() {
        return filename;
    }

    public String getSpriteFilename() {
        return spriteFilename;
    }

    public String getSoundFileName() {
        return soundFileName;
    }
}
