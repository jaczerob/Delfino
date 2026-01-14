package dev.jaczerob.delfino.maplestory.server.maps;

import java.awt.*;

public abstract class AbstractMapObject implements MapObject {
    private Point position = new Point();
    private int objectId;

    @Override
    public abstract MapObjectType getType();

    @Override
    public Point getPosition() {
        return new Point(position);
    }

    @Override
    public void setPosition(Point position) {
        this.position.move(position.x, position.y);
    }

    @Override
    public int getObjectId() {
        return objectId;
    }

    @Override
    public void setObjectId(int id) {
        this.objectId = id;
    }

    @Override
    public void nullifyPosition() {
        this.position = null;
    }
}
