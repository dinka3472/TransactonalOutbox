package org.example.usecasses.util;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class PartitionResolver {

    public static int forKey(Integer key, int partition) {
        return Math.floorMod(key.hashCode(), partition);
    }
}
