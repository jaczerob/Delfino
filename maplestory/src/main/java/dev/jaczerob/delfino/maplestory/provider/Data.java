package dev.jaczerob.delfino.maplestory.provider;

import dev.jaczerob.delfino.maplestory.provider.wz.DataType;

import java.util.List;

public interface Data extends DataEntity, Iterable<Data> {
    @Override
    String getName();

    DataType getType();

    List<Data> getChildren();

    Data getChildByPath(String path);

    Object getData();
}
