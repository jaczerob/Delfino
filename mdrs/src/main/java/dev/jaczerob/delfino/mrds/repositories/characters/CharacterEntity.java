package dev.jaczerob.delfino.mrds.repositories.characters;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Generated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "characters")

@NoArgsConstructor
@Getter
@Setter
public class CharacterEntity {
    @Generated
    @Id
    @Column(name = "id")
    private int id;
    @Column(name = "accountid")
    private int accountID;
    @Column(name = "world")
    private int world;
    @Column(name = "name")
    private String name;
    @Column(name = "level")
    private int level;
    @Column(name = "exp")
    private int exp;
    @Column(name = "gachaexp")
    private int gachaEXP;
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
    private int maxHP;
    @Column(name = "maxmp")
    private int maxMP;
    @Column(name = "meso")
    private int meso;
    @Column(name = "hpMpUsed")
    private int hpMPUsed;
    @Column(name = "job")
    private int job;
    @Column(name = "skincolor")
    private int skinColor;
    @Column(name = "gender")
    private int gender;
    @Column(name = "fame")
    private int fame;
    @Column(name = "fquest")
    private int fquest;
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
    private int spawnPoint;
    @Column(name = "gm")
    private int gm;
    @Column(name = "party")
    private int party;
    @Column(name = "buddyCapacity")
    private int buddyCapacity;
    @Column(name = "createdate")
    private Instant createdDate;
    @Column(name = "rank")
    private int rank;
    @Column(name = "rankMove")
    private int rankMove;
    @Column(name = "jobRank")
    private int jobRank;
    @Column(name = "jobRankMove")
    private int jobRankMove;
    @Column(name = "guildid")
    private int guildID;
    @Column(name = "guildrank")
    private int guildRank;
    @Column(name = "messengerid")
    private int messengerID;
    @Column(name = "messengerposition")
    private int messengerPosition;
    @Column(name = "mountlevel")
    private int mountLevel;
    @Column(name = "mountexp")
    private int mountEXP;
    @Column(name = "mounttiredness")
    private int mountTiredness;
    @Column(name = "omokwins")
    private int omokWins;
    @Column(name = "omoklosses")
    private int omokLosses;
    @Column(name = "omokties")
    private int omokTies;
    @Column(name = "matchcardwins")
    private int matchcardWins;
    @Column(name = "matchcardlosses")
    private int matchcardLosses;
    @Column(name = "matchcardties")
    private int matchCardTies;
    @Column(name = "MerchantMesos")
    private int merchantMesos;
    @Column(name = "HasMerchant")
    private int hasMerchane;
    @Column(name = "equipslots")
    private int equipSlots;
    @Column(name = "useslots")
    private int useSlots;
    @Column(name = "setupslots")
    private int setupSlots;
    @Column(name = "etcslots")
    private int etcSlots;
    @Column(name = "familyId")
    private int familyId;
    @Column(name = "monsterbookcover")
    private int monsterBookCover;
    @Column(name = "allianceRank")
    private int allianceRank;
    @Column(name = "vanquisherStage")
    private int vanquisherStage;
    @Column(name = "ariantPoints")
    private int ariantPoints;
    @Column(name = "dojoPoints")
    private int dojoPoints;
    @Column(name = "lastDojoStage")
    private int lastDojoStage;
    @Column(name = "finishedDojoTutorial")
    private int finishedDojoTutorial;
    @Column(name = "vanquisherKills")
    private int vanquisherKills;
    @Column(name = "summonValue")
    private int summonValue;
    @Column(name = "partnerId")
    private int partnerId;
    @Column(name = "marriageItemId")
    private int marriageItemId;
    @Column(name = "reborns")
    private int reborns;
    @Column(name = "PQPoints")
    private int pqPoints;
    @Column(name = "dataString")
    private String dataString;
    @Column(name = "lastLogoutTime")
    private Instant lastLogoutTime;
    @Column(name = "lastExpGainTime")
    private Instant lastExpGainTime;
    @Column(name = "partySearch")
    private int partySearch;
    @Column(name = "jailexpire")
    private int jailExpire;
}
