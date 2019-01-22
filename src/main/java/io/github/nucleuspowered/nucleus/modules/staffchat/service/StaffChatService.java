/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.staffchat.service;

import io.github.nucleuspowered.nucleus.api.chat.NucleusChatChannel;
import io.github.nucleuspowered.nucleus.api.service.NucleusStaffChatService;
import io.github.nucleuspowered.nucleus.modules.staffchat.StaffChatMessageChannel;

public class StaffChatService implements NucleusStaffChatService {

    @Override
    public NucleusChatChannel.StaffChat getStaffChat() {
        return StaffChatMessageChannel.getInstance();
    }
}
