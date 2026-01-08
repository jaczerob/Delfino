package dev.jaczerob.delfino.maplestory.repositories;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "characters")
public class CharacterEntity {
    @Id
    @Column(name = "id")
    private int id;
    @Column(name = "accountid")
    private int accountid;
    @Column(name = "world")
    private int world;
    @Column(name = "name")
    private String name;
    @Column(name = "level")
    private int level;
    @Column(name = "exp")
    private int exp;
    @Column(name = "gachaexp")
    private int gachaexp;
    @Column(name = "str")
    private int str;
    @Column(name = "dex")
    private int dex;
    @Column(name = "luk")
    private int luk;
    @Column(name = "int")
    private int int_;
    @Column(name = "hp")
    private int hp;
    @Column(name = "mp")
    private int mp;
    @Column(name = "maxhp")
    private int maxhp;
    @Column(name = "maxmp")
    private int maxmp;
    @Column(name = "meso")
    private int meso;
    @Column(name = "job")
    private int job;
    @Column(name = "skincolor")
    private int skincolor;
    @Column(name = "gender")
    private int gender;
    @Column(name = "fame")
    private int fame;
    @Column(name = "hair")
    private int hair;
    @Column(name = "face")
    private int face;
    @Column(name = "ap")
    private int ap;
    @Column(name = "sp")
    private String sp;
    @Column(name = "map")
    private int map;
    @Column(name = "spawnpoint")
    private int spawnpoint;
    @Column(name = "gm")
    private int gm;
    @Column(name = "rank")
    private int rank;
    @Column(name = "rankMove")
    private int rankMove;
    @Column(name = "jobRank")
    private int jobRank;
    @Column(name = "jobRankMove")
    private int jobRankMove;

    public CharacterEntity() {

    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAccountid() {
        return this.accountid;
    }

    public void setAccountid(int accountid) {
        this.accountid = accountid;
    }

    public int getWorld() {
        return this.world;
    }

    public void setWorld(int world) {
        this.world = world;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getExp() {
        return this.exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public int getGachaexp() {
        return this.gachaexp;
    }

    public void setGachaexp(int gachaexp) {
        this.gachaexp = gachaexp;
    }

    public int getStr() {
        return this.str;
    }

    public void setStr(int str) {
        this.str = str;
    }

    public int getDex() {
        return this.dex;
    }

    public void setDex(int dex) {
        this.dex = dex;
    }

    public int getLuk() {
        return this.luk;
    }

    public void setLuk(int luk) {
        this.luk = luk;
    }

    public int getInt_() {
        return this.int_;
    }

    public void setInt_(int int_) {
        this.int_ = int_;
    }

    public int getHp() {
        return this.hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getMp() {
        return this.mp;
    }

    public void setMp(int mp) {
        this.mp = mp;
    }

    public int getMaxhp() {
        return this.maxhp;
    }

    public void setMaxhp(int maxhp) {
        this.maxhp = maxhp;
    }

    public int getMaxmp() {
        return this.maxmp;
    }

    public void setMaxmp(int maxmp) {
        this.maxmp = maxmp;
    }

    public int getMeso() {
        return this.meso;
    }

    public void setMeso(int meso) {
        this.meso = meso;
    }

    public int getJob() {
        return this.job;
    }

    public void setJob(int job) {
        this.job = job;
    }

    public int getSkincolor() {
        return this.skincolor;
    }

    public void setSkincolor(int skincolor) {
        this.skincolor = skincolor;
    }

    public int getGender() {
        return this.gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public int getFame() {
        return this.fame;
    }

    public void setFame(int fame) {
        this.fame = fame;
    }

    public int getHair() {
        return this.hair;
    }

    public void setHair(int hair) {
        this.hair = hair;
    }

    public int getFace() {
        return this.face;
    }

    public void setFace(int face) {
        this.face = face;
    }

    public int getAp() {
        return this.ap;
    }

    public void setAp(int ap) {
        this.ap = ap;
    }

    public String getSp() {
        return this.sp;
    }

    public void setSp(String sp) {
        this.sp = sp;
    }

    public int getMap() {
        return this.map;
    }

    public void setMap(int map) {
        this.map = map;
    }

    public int getSpawnpoint() {
        return this.spawnpoint;
    }

    public void setSpawnpoint(int spawnpoint) {
        this.spawnpoint = spawnpoint;
    }

    public int getGm() {
        return this.gm;
    }

    public void setGm(int gm) {
        this.gm = gm;
    }

    public int getRank() {
        return this.rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getRankMove() {
        return this.rankMove;
    }

    public void setRankMove(int rankMove) {
        this.rankMove = rankMove;
    }

    public int getJobRank() {
        return this.jobRank;
    }

    public void setJobRank(int jobRank) {
        this.jobRank = jobRank;
    }

    public int getJobRankMove() {
        return this.jobRankMove;
    }

    public void setJobRankMove(int jobRankMove) {
        this.jobRankMove = jobRankMove;
    }
}
