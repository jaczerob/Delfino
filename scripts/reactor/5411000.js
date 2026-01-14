function act() {
    rm.changeMusic("Bgm09/TimeAttack");
    rm.spawnMonster(9420513, -146, 225);
    rm.getEventInstance().setIntProperty("boss", 1);
    rm.mapMessage(5, "As you wish, here comes Capt Latanica.");
}