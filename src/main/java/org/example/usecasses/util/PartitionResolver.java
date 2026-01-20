package org.example.usecasses.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PartitionResolver {

    public int forKey(Integer key, int partitions) {
        return Math.floorMod(key.hashCode(), partitions);
    }
}
