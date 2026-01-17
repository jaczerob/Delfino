package dev.jaczerob.delfino.maplestory.scripts;

import dev.jaczerob.delfino.maplestory.server.maps.MapleMap;
import moe.maple.api.script.model.object.FieldObject;
import moe.maple.api.script.model.object.field.MobObject;
import moe.maple.api.script.model.object.user.UserObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ScriptFieldObject implements FieldObject<MapleMap> {
    private final MapleMap map;

    public ScriptFieldObject(final MapleMap map) {
        this.map = map;
    }

    @Override
    public int getId() {
        return this.map.getId();
    }

    @Override
    public Collection<UserObject> getUsers() {
        final var users = new ArrayList<UserObject>();
        this.map.getAllPlayers().stream()
                .map(ScriptUserObject::new)
                .forEach(users::add);
        return users;
    }

    @Override
    public Collection<MobObject> getMobs() {
        return List.of();
    }

    @Override
    public long getMobHp(int mobId) {
        return this.map.getMonsterById(mobId).getHp();
    }

    @Override
    public int countUserInArea(String areaName) {
        return this.map.countPlayers();
    }

    @Override
    public int countMaleInArea(String areaName) {
        return (int) this.map.getAllPlayers().stream().filter(player -> player.getGender() == 0).count();
    }

    @Override
    public int countFemaleInArea(String areaName) {
        return (int) this.map.getAllPlayers().stream().filter(player -> player.getGender() == 1).count();
    }

    @Override
    public boolean enablePortal(String portalName, boolean enable) {
        this.map.getPortal(portalName).setPortalState(enable);
        return true;
    }

    @Override
    public boolean summonMob(int templateId, int xPos, int yPos) {
        return false;
    }

    @Override
    public void notice(Integer type, String... message) {

    }

    @Override
    public boolean isItemInArea(String areaName, int itemID) {
        return this.map.getItems().stream()
                .anyMatch(item -> item.getObjectId() == itemID);
    }

    @Override
    public boolean removeMob(int mobId) {
        this.map.killMonster(mobId);
        return true;
    }

    @Override
    public MapleMap get() {
        return this.map;
    }
}
