package dev.jaczerob.delfino.maplestory.server.events.gm;

import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.provider.DataProvider;
import dev.jaczerob.delfino.maplestory.provider.DataProviderFactory;
import dev.jaczerob.delfino.maplestory.provider.DataTool;
import dev.jaczerob.delfino.maplestory.provider.wz.WZFiles;
import dev.jaczerob.delfino.maplestory.server.TimerManager;
import dev.jaczerob.delfino.maplestory.server.maps.MapleMap;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.maplestory.tools.Randomizer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author FloppyDisk
 */
public final class OxQuiz {
    private int round = 1;
    private int question = 1;
    private MapleMap map = null;
    private final int expGain = 200;
    private static final DataProvider stringData = DataProviderFactory.getDataProvider(WZFiles.ETC);

    public OxQuiz(MapleMap map) {
        this.map = map;
        this.round = Randomizer.nextInt(9);
        this.question = 1;
    }

    private boolean isCorrectAnswer(Character chr, int answer) {
        double x = chr.getPosition().getX();
        double y = chr.getPosition().getY();
        if ((x > -234 && y > -26 && answer == 0) || (x < -234 && y > -26 && answer == 1)) {
            chr.dropMessage("Correct!");
            return true;
        }
        return false;
    }

    public void sendQuestion() {
        int gm = 0;
        for (Character mc : map.getCharacters()) {
            if (mc.gmLevel() > 1) {
                gm++;
            }
        }
        final int number = gm;
        map.broadcastMessage(ChannelPacketCreator.getInstance().showOXQuiz(round, question, true));
        TimerManager.getInstance().schedule(() -> {
            map.broadcastMessage(ChannelPacketCreator.getInstance().showOXQuiz(round, question, true));
            List<Character> chars = new ArrayList<>(map.getCharacters());

            for (Character chr : chars) {
                if (chr != null) // make sure they aren't null... maybe something can happen in 12 seconds.
                {
                    if (!isCorrectAnswer(chr, getOXAnswer(round, question)) && !chr.isGM()) {
                        chr.changeMap(chr.getMap().getReturnMap());
                    } else {
                        chr.gainExp(expGain, true, true);
                    }
                }
            }
            //do question
            if ((round == 1 && question == 29) || ((round == 2 || round == 3) && question == 17) || ((round == 4 || round == 8) && question == 12) || (round == 5 && question == 26) || (round == 9 && question == 44) || ((round == 6 || round == 7) && question == 16)) {
                question = 100;
            } else {
                question++;
            }
            //send question
            if (map.getCharacters().size() - number <= 2) {
                map.broadcastMessage(ChannelPacketCreator.getInstance().serverNotice(6, "The event has ended"));
                map.getPortal("join00").setPortalStatus(true);
                map.setOx(null);
                map.setOxQuiz(false);
                //prizes here
                return;
            }
            sendQuestion();
        }, 30000); // Time to answer = 30 seconds ( Ox Quiz packet shows a 30 second timer.
    }

    private static int getOXAnswer(int imgdir, int id) {
        return DataTool.getInt(stringData.getData("OXQuiz.img").getChildByPath("" + imgdir + "").getChildByPath("" + id + "").getChildByPath("a"));
    }
}
