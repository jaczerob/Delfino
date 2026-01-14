package dev.jaczerob.delfino.maplestory.server.maps;

import dev.jaczerob.delfino.maplestory.tools.Pair;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Lerk
 * @author Ronan
 */
public class ReactorStats {
    private Point tl;
    private Point br;
    private final Map<Byte, List<StateData>> stateInfo = new HashMap<>();
    private final Map<Byte, Integer> timeoutInfo = new HashMap<>();

    public void setTL(Point tl) {
        this.tl = tl;
    }

    public void setBR(Point br) {
        this.br = br;
    }

    public Point getTL() {
        return tl;
    }

    public Point getBR() {
        return br;
    }

    public void addState(byte state, List<StateData> data, int timeOut) {
        stateInfo.put(state, data);
        if (timeOut > -1) {
            timeoutInfo.put(state, timeOut);
        }
    }

    public void addState(byte state, int type, Pair<Integer, Integer> reactItem, byte nextState, int timeOut, byte canTouch) {
        List<StateData> data = new ArrayList<>();
        data.add(new StateData(type, reactItem, null, nextState));
        stateInfo.put(state, data);
    }

    public int getTimeout(byte state) {
        Integer i = timeoutInfo.get(state);
        return (i == null) ? -1 : i;
    }

    public byte getTimeoutState(byte state) {
        return stateInfo.get(state).get(stateInfo.get(state).size() - 1).getNextState();
    }

    public byte getStateSize(byte state) {
        return (byte) stateInfo.get(state).size();
    }

    public byte getNextState(byte state, byte index) {
        if (stateInfo.get(state) == null || stateInfo.get(state).size() < (index + 1)) {
            return -1;
        }
        StateData nextState = stateInfo.get(state).get(index);
        if (nextState != null) {
            return nextState.getNextState();
        } else {
            return -1;
        }
    }

    public List<Integer> getActiveSkills(byte state, byte index) {
        StateData nextState = stateInfo.get(state).get(index);
        if (nextState != null) {
            return nextState.getActiveSkills();
        } else {
            return null;
        }
    }

    public int getType(byte state) {
        List<StateData> list = stateInfo.get(state);
        if (list != null) {
            return list.get(0).getType();
        } else {
            return -1;
        }
    }

    public Pair<Integer, Integer> getReactItem(byte state, byte index) {
        StateData nextState = stateInfo.get(state).get(index);
        if (nextState != null) {
            return nextState.getReactItem();
        } else {
            return null;
        }
    }


    public static class StateData {
        private final int type;
        private final Pair<Integer, Integer> reactItem;
        private final List<Integer> activeSkills;
        private final byte nextState;

        public StateData(int type, Pair<Integer, Integer> reactItem, List<Integer> activeSkills, byte nextState) {
            this.type = type;
            this.reactItem = reactItem;
            this.activeSkills = activeSkills;
            this.nextState = nextState;
        }

        private int getType() {
            return type;
        }

        private byte getNextState() {
            return nextState;
        }

        private Pair<Integer, Integer> getReactItem() {
            return reactItem;
        }

        private List<Integer> getActiveSkills() {
            return activeSkills;
        }
    }
}
