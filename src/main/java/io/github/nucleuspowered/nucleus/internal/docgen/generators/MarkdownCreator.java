/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.docgen.generators;

import com.google.common.collect.Lists;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;

class MarkdownCreator {

    static <T> List<String> createTable(List<T> objs, LinkedHashMap<String, Function<T, String>> table) {
        List<String> lss = Lists.newArrayList();
        StringBuilder sb = new StringBuilder("|");
        StringBuilder sb2 = new StringBuilder("|");
        table.forEach((k, v) -> {
            sb.append(k).append("|");
            sb2.append("---").append("|");
        });
        lss.add(sb.toString());
        lss.add(sb2.toString());

        objs.forEach(entry -> {
            final StringBuilder entryBuilder = new StringBuilder("|");
            table.forEach((k, v) -> entryBuilder.append(v.apply(entry)).append("|"));
            lss.add(entryBuilder.toString());
        });

        return lss;
    }
}
