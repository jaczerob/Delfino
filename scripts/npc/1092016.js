
/*
        Author : XxOsirisxX (BubblesDev)
        NPC Name:               Shiny Stone
*/

function start() {
    if (cm.isQuestStarted(2166)) {
        cm.sendNext("It's a beautiful, shiny rock. I can feel the mysterious power surrounding it.");
        cm.completeQuest(2166);
    } else {
        cm.sendNext("I touched the shiny rock with my hand, and I felt a mysterious power flowing into my body.");
    }
    cm.dispose();
}