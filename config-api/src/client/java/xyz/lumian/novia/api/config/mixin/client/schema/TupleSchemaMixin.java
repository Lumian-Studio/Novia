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

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.lumian.novia.api.config.PropertyId;
import xyz.lumian.novia.api.config.client.document.ComponentStyle;
import xyz.lumian.novia.api.config.client.gui.schema.ISchemaFactory;
import xyz.lumian.novia.api.config.client.gui.schema.IComponentFactory;
import xyz.lumian.novia.api.config.client.gui.schema.component.SchemaEditButton;
import xyz.lumian.novia.api.config.client.gui.schema.component.SchemaList;
import xyz.lumian.novia.api.config.schema.IPropertySchema;
import xyz.lumian.novia.api.config.schema.builtin.TupleSchema;

import java.util.*;



//**********************************************************************************************************************
@Mixin(TupleSchema.class)
public abstract class TupleSchemaMixin
    implements ISchemaFactory
{
    //******************************************************************************************************************
    @Shadow(remap = false)
    @Final
    private ImmutableList<IPropertySchema> schemas;
    
    //******************************************************************************************************************
    @Override
    public @Nullable IComponentFactory<?> novia$getFactory(final PropertyId id, final ComponentStyle style)
    {
        final List<Optional<IComponentFactory<?>>> factories = new ArrayList<>();
        
        for (final var schema : this.schemas)
        {
            if (!(schema instanceof ISchemaFactory sub_factory))
            {
                factories.add(Optional.empty());
                continue;
            }
            
            final IComponentFactory<?> widget_factory = sub_factory.novia$getFactory(id, style);
            
            if (widget_factory == null)
            {
                factories.add(Optional.empty());
                continue;
            }
            
            factories.add(Optional.of(widget_factory));
        }
        
        if (factories.isEmpty())
        {
            return null;
        }
        
        return (screen -> new SchemaEditButton(id, new SchemaList(screen, id.providerId(), factories), screen));
    }
}
