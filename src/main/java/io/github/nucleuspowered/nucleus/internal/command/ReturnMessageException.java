/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.command;

import io.github.nucleuspowered.nucleus.Nucleus;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.TextMessageException;

/**
 * Simple exception to only send back an error message to the user. Useful in optionals.
 */
public class ReturnMessageException extends TextMessageException {

    public ReturnMessageException(Text text) {
        super(text);
    }

    public ReturnMessageException(Text text, Throwable inner) {
        super(text, inner);
    }

    public static ReturnMessageException fromKey(String loc, String... arg) {
        return new ReturnMessageException(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat(loc, arg));
    }

    public static ReturnMessageException fromKeyText(String loc, Text... arg) {
        return new ReturnMessageException(Nucleus.getNucleus().getMessageProvider().getTextMessageWithTextFormat(loc, arg));
    }
}
