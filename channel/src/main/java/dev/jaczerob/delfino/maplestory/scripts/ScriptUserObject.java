package dev.jaczerob.delfino.maplestory.scripts;

import dev.jaczerob.delfino.maplestory.client.Character;
import moe.maple.api.script.model.helper.Exchange;
import moe.maple.api.script.model.object.FieldObject;
import moe.maple.api.script.model.object.GuildObject;
import moe.maple.api.script.model.object.PartyObject;
import moe.maple.api.script.model.object.user.InventorySlotObject;
import moe.maple.api.script.model.object.user.QuestHolderObject;
import moe.maple.api.script.model.object.user.UserObject;
import moe.maple.api.script.util.tuple.Tuple;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

public class ScriptUserObject implements UserObject<Character> {
    private final Character character;

    public ScriptUserObject(final Character character) {
        this.character = character;
    }

    @Override
    public int getId() {
        return this.character.getId();
    }

    @Override
    public String getName() {
        return this.character.getName();
    }

    @Override
    public Optional<GuildObject> getGuild() {
        return Optional.empty();
    }

    @Override
    public Optional<PartyObject> getParty() {
        return Optional.empty();
    }

    @Override
    public QuestHolderObject getQuestHolder() {
        return null;
    }

    @Override
    public int isCreateGuildPossible(int cost) {
        return 0;
    }

    @Override
    public void createNewGuild(int cost) {

    }

    @Override
    public boolean removeGuild(int cost) {
        return false;
    }

    @Override
    public int getBuddyCapacity() {
        return this.character.getBuddylist().getCapacity();
    }

    @Override
    public boolean increaseBuddyCapacity(int amount, int cost) {
        return false;
    }

    @Override
    public String getScriptVariable(String key) {
        return "";
    }

    @Override
    public boolean setScriptVariable(String key, String value) {
        return false;
    }

    @Override
    public void talkTo(String scriptName) {

    }

    @Override
    public void talkTo(int npcId) {

    }

    @Override
    public void openShop(int shopId) {

    }

    @Override
    public boolean transferField(int fieldId) {
        this.character.changeMap(fieldId);
        return true;
    }

    @Override
    public boolean transferField(int fieldId, int spawnPoint) {
        this.character.changeMap(fieldId);
        return true;
    }

    @Override
    public boolean transferField(int fieldId, String spawnPoint) {
        this.character.changeMap(fieldId);
        return true;
    }

    @Override
    public boolean learnSkill(int skillId, int level, int mastery) {
        return false;
    }

    @Override
    public boolean forgetSkill(int skillId) {
        return false;
    }

    @Override
    public boolean giveBuffItem(int buffItemId) {
        return false;
    }

    @Override
    public boolean giveBuffSkill(int skillId) {
        return false;
    }

    @Override
    public boolean hireTutor() {
        return false;
    }

    @Override
    public boolean fireTutor() {
        return false;
    }

    @Override
    public boolean hasTutor() {
        return false;
    }

    @Override
    public void tutorMessage(int value, int duration) {

    }

    @Override
    public void tutorMessage(String value, int width, int duration) {

    }

    @Override
    public void setStandAloneMode(boolean set) {

    }

    @Override
    public void setDirectionMode(boolean set) {

    }

    @Override
    public int getGender() {
        return this.character.getGender();
    }

    @Override
    public int getHair() {
        return this.character.getHair();
    }

    @Override
    public int getFace() {
        return this.character.getFace();
    }

    @Override
    public boolean setHair(int hairId) {
        return false;
    }

    @Override
    public boolean setFace(int faceId) {
        return false;
    }

    @Override
    public int getSkin() {
        return this.character.getSkinColor().getId();
    }

    @Override
    public int getStrength() {
        return this.character.getStr();
    }

    @Override
    public int getDexterity() {
        return this.character.getDex();
    }

    @Override
    public int getIntelligence() {
        return this.character.getInt();
    }

    @Override
    public int getLuck() {
        return this.character.getLuk();
    }

    @Override
    public int getFame() {
        return this.character.getFame();
    }

    @Override
    public int getMoney() {
        return this.character.getMeso();
    }

    @Override
    public boolean increaseMoney(int amount) {
        this.character.gainMeso(amount);
        return true;
    }

    @Override
    public int getLevel() {
        return this.character.getLevel();
    }

    @Override
    public long getExperience() {
        return this.character.getExp();
    }

    @Override
    public boolean increaseExp(int amount, boolean quest) {
        this.character.gainExp(amount);
        return true;
    }

    @Override
    public int getAbilityPoints() {
        return this.character.getRemainingAp();
    }

    @Override
    public boolean increaseAbilityPoints(int amount) {
        return false;
    }

    @Override
    public int getSkillPoints(int tier) {
        return this.character.getRemainingSp();
    }

    @Override
    public int getSkillPoints() {
        return this.character.getRemainingSp();
    }

    @Override
    public boolean increaseSkillPoints(int amount, int tier) {
        return false;
    }

    @Override
    public boolean increaseSkillPoints(int amount) {
        return false;
    }

    @Override
    public boolean setJob(short jobCode, boolean isJobAdvancement) {
        return false;
    }

    @Override
    public int getJobId() {
        return this.character.getJob().getId();
    }

    @Override
    public int resetAp(int remain) {
        return 0;
    }

    @Override
    public void openSkillGuide() {

    }

    @Override
    public void openClassCompetitionPage() {

    }

    @Override
    public int getChannelId() {
        return this.character.getClient().getChannel();
    }

    @Override
    public long getHealthCurrent() {
        return this.character.getHp();
    }

    @Override
    public int getManaCurrent() {
        return this.character.getMp();
    }

    @Override
    public long getHealthMax() {
        return this.character.getMaxHp();
    }

    @Override
    public int getManaMax() {
        return this.character.getMaxMp();
    }

    @Override
    public boolean increaseHealth(int amountToHeal) {
        return false;
    }

    @Override
    public boolean increaseMana(int amountToHeal) {
        return false;
    }

    @Override
    public boolean decreaseHealth(int amountToReduce) {
        this.character.addHP(-amountToReduce);
        return true;
    }

    @Override
    public boolean decreaseMana(int amountToReduce) {
        this.character.addMP(-amountToReduce);
        return true;
    }

    @Override
    public boolean increaseHealthMax(int amountToIncrease) {
        this.character.addMaxHP(amountToIncrease);
        return true;
    }

    @Override
    public boolean increaseManaMax(int amountToIncrease) {
        this.character.addMaxMP(amountToIncrease);
        return true;
    }

    @Override
    public int getObjectId() {
        return this.character.getId();
    }

    @Override
    public int getX() {
        return this.character.getPosition().x;
    }

    @Override
    public int getY() {
        return this.character.getPosition().y;
    }

    @Override
    public Optional<? extends FieldObject> getField() {
        return Optional.of(new ScriptFieldObject(this.character.getMap()));
    }

    @Override
    public boolean exchange(Exchange exchange) {
        return false;
    }

    @Override
    public boolean addItem(int itemTemplateId, int count) {
        return false;
    }

    @Override
    public boolean addItemAll(int... itemTemplateId) {
        return false;
    }

    @Override
    public boolean addItemAll(Collection<Tuple<Integer, Integer>> itemTemplateIdAndCount) {
        return false;
    }

    @Override
    public boolean removeItem(int itemTemplateId) {
        return false;
    }

    @Override
    public boolean removeItem(int itemTemplateId, int count) {
        return false;
    }

    @Override
    public boolean removeItemAll(int... itemTemplateId) {
        return false;
    }

    @Override
    public boolean removeItemAll(Collection<Tuple<Integer, Integer>> itemTemplateIdAndCount) {
        return false;
    }

    @Override
    public boolean removeSlot(int tab, short position) {
        return false;
    }

    @Override
    public int getItemCount(int itemTemplateId) {
        return 0;
    }

    @Override
    public int getSlotCount(int inventoryType) {
        return 0;
    }

    @Override
    public boolean increaseSlotCount(int inventoryType, int howMany) {
        return false;
    }

    @Override
    public Optional<InventorySlotObject> getItem(int inventoryType, short slot) {
        return Optional.empty();
    }

    @Override
    public Stream<InventorySlotObject> streamItems(int inventoryType) {
        return Stream.empty();
    }

    @Override
    public Character get() {
        return this.character;
    }
}
