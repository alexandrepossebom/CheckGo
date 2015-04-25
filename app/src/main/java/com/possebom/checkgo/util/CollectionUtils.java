package com.possebom.checkgo.util;

import java.util.Collection;

/**
 * Created by alexandre on 15/04/15.
 */
public final class CollectionUtils {

    public static boolean isEmpty(final Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

}