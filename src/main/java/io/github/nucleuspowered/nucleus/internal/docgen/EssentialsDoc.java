/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.docgen;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.List;

@ConfigSerializable
public class EssentialsDoc {

    @Setting
    private List<String> essentialsCommands;

    @Setting
    private List<String> nucleusEquiv;

    @Setting
    private boolean isExact;

    @Setting
    private String notes;

    public List<String> getEssentialsCommands() {
        return essentialsCommands;
    }

    public void setEssentialsCommands(List<String> essentialsCommands) {
        this.essentialsCommands = essentialsCommands;
    }

    public List<String> getNucleusEquiv() {
        return nucleusEquiv;
    }

    public void setNucleusEquiv(List<String> nucleusEquiv) {
        this.nucleusEquiv = nucleusEquiv;
    }

    public boolean isExact() {
        return isExact;
    }

    public void setExact(boolean exact) {
        isExact = exact;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
