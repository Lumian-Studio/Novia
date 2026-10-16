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
package xyz.lumialights.novia.api.config.client.gui.schema.component;

import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import xyz.lumialights.novia.api.config.PropertyId;
import xyz.lumialights.novia.api.config.client.ConfigApiLangClient;
import xyz.lumialights.novia.api.config.client.gui.ConfigScreen;
import xyz.lumialights.novia.api.core.serialisation.Value;
import xyz.lumialights.novia.api.gui.component.integration.IStatefulComponent;
import xyz.lumialights.novia.api.gui.event.GuiEvent;

import java.util.*;



//**********************************************************************************************************************
public class SchemaEditButton
    extends SchemaButton
{
    //******************************************************************************************************************
    public GuiEvent.Simple propertyChanged = new GuiEvent.Simple();
    
    //------------------------------------------------------------------------------------------------------------------
    private final PropertyId         id;
    private final IStatefulComponent component;
    private final ConfigScreen       screen;
    
    //******************************************************************************************************************
    public SchemaEditButton(final @NotNull PropertyId         propertyId,
                            final @NotNull IStatefulComponent component,
                            final @NotNull ConfigScreen       screen)
    {
        super(ConfigApiLangClient.CONFIG_EDIT_BUTTON_TEXT);
        
        this.id        = propertyId;
        this.component = component;
        this.screen    = screen;
        
        this.clicked.subscribe((sender, e) -> this.openScreen());
    }
    
    //==================================================================================================================
    @Override public @NotNull Value           getValue()       { return this.component.getValue(); }
    @Override public @NotNull GuiEvent.Simple getChangeEvent() { return this.propertyChanged; }
    
    //==================================================================================================================
    @Override public void setValue(final @NotNull Value value) { this.component.setValue(value); }
    
    //==================================================================================================================
    private void openScreen()
    {
        this.screen.showEditScreen(
            this.id,
            Text.literal(Objects.requireNonNull(this.id.pointer().getName())),
            this.component);
    }
}
