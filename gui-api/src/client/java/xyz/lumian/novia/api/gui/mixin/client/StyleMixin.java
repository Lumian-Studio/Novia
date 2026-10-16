/**
           .-----------------. .----------------.  .----------------.  .----------------.  .----------------.
          | .--------------. || .--------------. || .--------------. || .--------------. || .--------------. |
          | | ____  _____  | || |     ____     | || | ____   ____  | || |     _____    | || |      __      | |
          | ||_   \|_   _| | || |   .'    `.   | || ||_  _| |_  _| | || |    |_   _|   | || |     /  \     | |
          | |  |   \ | |   | || |  /  .--.  \  | || |  \ \   / /   | || |      | |     | || |    / /\ \    | |
          | |  | |\ \| |   | || |  | |    | |  | || |   \ \ / /    | || |      | |     | || |   / ____ \   | |
          | | _| |_\   |_  | || |  \  `--'  /  | || |    \ ' /     | || |     _| |_    | || | _/ /    \ \_ | |
          | ||_____|\____| | || |   `.____.'   | || |     \_/      | || |    |_____|   | || ||____|  |____|| |
          | |              | || |              | || |              | || |              | || |              | |
          | '--------------' || '--------------' || '--------------' || '--------------' || '--------------' |
           '----------------'  '----------------'  '----------------'  '----------------'  '----------------'

    MIT License

    Copyright (c) 2025 LumiaLights

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
 */
package xyz.lumian.novia.api.gui.mixin.client;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.lumian.novia.api.gui.impl.StyleAccessor;

import java.util.Optional;



//**********************************************************************************************************************
@Mixin(Style.class)
public abstract class StyleMixin
    implements StyleAccessor
{
    //******************************************************************************************************************
    @Shadow @Final @Nullable private FontDescription font;
    @Shadow @Final @Nullable private Boolean         bold;
    @Shadow @Final @Nullable private Boolean         italic;
    @Shadow @Final @Nullable private Boolean         underlined;
    @Shadow @Final @Nullable private Boolean         strikethrough;
    @Shadow @Final @Nullable private Boolean         obfuscated;

    //******************************************************************************************************************
    @Override public @Nullable FontDescription novia$getFontId() { return this.font; }

    @Override
    public @Nullable Optional<Boolean> novia$getFormatFlag(final ChatFormatting formatting)
    {
        return Optional.ofNullable(switch (formatting)
        {
            case BOLD          -> this.bold;
            case ITALIC        -> this.italic;
            case UNDERLINE     -> this.underlined;
            case STRIKETHROUGH -> this.strikethrough;
            case OBFUSCATED    -> this.obfuscated;

            default -> throw new IllegalArgumentException("invalid format flag " + formatting.getSerializedName());
        });
    }

    //==================================================================================================================
    @Override public boolean novia$isFontSet() { return (this.font != null); }

    //==================================================================================================================
    @Override public @Nullable Boolean novia$isBold()          { return this.bold; }
    @Override public @Nullable Boolean novia$isItalic()        { return this.italic; }
    @Override public @Nullable Boolean novia$isUnderlined()    { return this.underlined; }
    @Override public @Nullable Boolean novia$isStrikethrough() { return this.strikethrough; }
    @Override public @Nullable Boolean novia$isObfuscated()    { return this.obfuscated; }
}
