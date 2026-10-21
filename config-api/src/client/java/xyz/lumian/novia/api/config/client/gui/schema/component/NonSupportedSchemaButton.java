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
package xyz.lumian.novia.api.config.client.gui.schema.component;

import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import xyz.lumian.novia.api.config.ConfigManager;
import xyz.lumian.novia.api.config.client.ConfigApiLangClient;
import xyz.lumian.novia.api.core.Novia;
import xyz.lumian.novia.api.core.serialisation.Value;
import xyz.lumian.novia.api.gui.component.integration.IStatefulComponent;
import xyz.lumian.novia.api.gui.component.provided.NVSimpleButton;
import xyz.lumian.novia.api.gui.event.GuiEvent;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;



//**********************************************************************************************************************
public class NonSupportedSchemaButton
    extends NVSimpleButton
    implements IStatefulComponent
{
    //******************************************************************************************************************
    private final Identifier      providerId;
    private final GuiEvent.Simple na = new GuiEvent.Simple();
    
    private Value cached = new Value();
    
    //******************************************************************************************************************
    public NonSupportedSchemaButton(final Identifier providerId)
    {
        super(ConfigApiLangClient.CONFIG_EDIT_IN_CONFIG_NOTICE, ConfigApiLangClient.CONFIG_EDIT_IN_CONFIG_NOTICE);

        this.providerId = Objects.requireNonNull(providerId, "provider id must not be null");

        final Path path = ConfigManager.getInstance().getFileForId(providerId);
        this.setTooltip(Tooltip.create(Component.nullToEmpty(path.getFileName().toString())));
        
        this.clicked.subscribe((_, _) -> this.openFile());
    }
    
    //==================================================================================================================
    @Override public Value           getValue()       { return this.cached; }
    @Override public GuiEvent.Simple getChangeEvent() { return na; }
    
    //==================================================================================================================
    @Override public void setValue(final Value value) { this.cached = value; }
    
    //==================================================================================================================
    @Override public void mute()   {}
    @Override public void unmute() {}
    
    //==================================================================================================================
    private void openFile()
    {
        final Path file = ConfigManager.getInstance().getFileForId(this.providerId);
        
        if (!Files.exists(file))
        {
            return;
        }
        
        try
        {
            Util.getPlatform().openFile(file.toFile());
        }
        catch (final Exception ex)
        {
            Novia.LOGGER.error("Could not open config file '{}' due to: {}", file, ex);
        }
    }
}
