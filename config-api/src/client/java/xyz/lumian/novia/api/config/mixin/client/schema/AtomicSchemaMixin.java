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
package xyz.lumian.novia.api.config.mixin.client.schema;

import com.mojang.datafixers.util.Pair;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.lumian.novia.api.config.PropertyId;
import xyz.lumian.novia.api.config.client.document.ComponentStyle;
import xyz.lumian.novia.api.config.client.gui.schema.IComponentFactory;
import xyz.lumian.novia.api.config.client.gui.schema.ISchemaFactory;
import xyz.lumian.novia.api.config.client.gui.schema.component.SchemaEnumButton;
import xyz.lumian.novia.api.config.client.gui.schema.component.SchemaLabel;
import xyz.lumian.novia.api.config.schema.builtin.atomic.BaseAtomicSchema;
import xyz.lumian.novia.api.core.serialisation.Value;
import xyz.lumian.novia.api.core.util.NormalisedRange;
import xyz.lumian.novia.api.gui.GuiApiLang;
import xyz.lumian.novia.api.gui.component.provided.NVCheckBox;
import xyz.lumian.novia.api.gui.component.provided.NVNumericBox;
import xyz.lumian.novia.api.gui.component.provided.NVSlider;
import xyz.lumian.novia.api.gui.component.provided.NVTextBox;
import xyz.lumian.novia.api.gui.geometry.Restrainer;

import java.text.DecimalFormat;
import java.util.*;



//**********************************************************************************************************************
@Mixin(BaseAtomicSchema.class)
public abstract class AtomicSchemaMixin
    implements ISchemaFactory
{
    //******************************************************************************************************************
    @Shadow(remap = false)
    public abstract Value.Type getType();
    
    //==================================================================================================================
    @Override
    public @Nullable IComponentFactory<?> novia$getFactory(final PropertyId id, final ComponentStyle style)
    {
        return switch (this.getType())
        {
            case NUMBER  ->
            {
                final NormalisedRange range     = new NormalisedRange(
                    style.getProperty("min",  Number.class).orElse(-Double.MAX_VALUE).doubleValue(),
                    style.getProperty("max",  Number.class).orElse( Double.MAX_VALUE).doubleValue(),
                    style.getProperty("step", Number.class).orElse(1.0)              .doubleValue(),
                    style.getProperty("skew", Number.class).orElse(1.0)              .doubleValue());
                final String          format    = style.getProperty("format", String.class).orElse(null);
                final DecimalFormat   formatter = (format != null
                    ? new DecimalFormat(format)
                    : NVNumericBox.DEFAULT_FORMAT_SUPPLIER.get());
                
                yield ComponentStyle.Variant
                    .create("box",   (() -> (_ -> Util.make(new NVNumericBox(),(box ->
                    {
                        box.range       .set(range);
                        box.numberFormat.set(formatter);
                    })))))
                    .ifVar("slider", (() ->
                    {
                        final NVSlider.TextProvider text_provider = (format != null
                            ? (slider -> Component.nullToEmpty(formatter.format(slider.getValueAsDouble())))
                            : NVSlider.DEFAULT_DISPLAY_TEXT_PROVIDER);
                        
                        return (_ -> Util.make(new NVSlider(), (slider ->
                        {
                            slider.range              .set(range);
                            slider.displayTextProvider.set(text_provider);
                        })));
                    }))
                    .build(style);
            }
            
            case BOOLEAN -> ComponentStyle.Variant
                .create("checkbox", (() -> (_ ->
                {
                    final NVCheckBox component = new NVCheckBox();
                    component.setRestrainer(Restrainer.max(12, 12));
                    return component;
                })))
                .ifVar ("button",   (() ->
                {
                    final List<Pair<Component, Value>> values = List.of(
                        Pair.of(
                            style.labels().getOrDefault(Value.FALSE, GuiApiLang.GUI_BOOLEAN_VALUE_FALSE),
                            Value.FALSE),
                        Pair.of(
                            style.labels().getOrDefault(Value.TRUE, GuiApiLang.GUI_BOOLEAN_VALUE_TRUE),
                            Value.TRUE));
                    
                    return (_ -> new SchemaEnumButton(values));
                }))
                .build(style);
            
            case STRING  -> (_ -> new NVTextBox());
            case VOID    -> (_ -> new SchemaLabel(Component.literal("null")
                .setStyle(Style.EMPTY.withUnderlined(true))));
            
            /*
            case LIST -> (screen -> new SchemaEditButton(id, new SchemaList(screen, id.providerId(), )))
            */
            
            case MAP, LIST -> null;
        };
    }
}
