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
package xyz.lumialights.novia.api.config.client.gui;

import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import xyz.lumialights.novia.api.GuiApiLang;
import xyz.lumialights.novia.api.config.PropertyId;
import xyz.lumialights.novia.api.gui.canvas.Canvas;
import xyz.lumialights.novia.api.gui.component.GuiComponent;
import xyz.lumialights.novia.api.gui.component.StatefulGuiComponent;
import xyz.lumialights.novia.api.gui.component.integration.IStatefulComponent;
import xyz.lumialights.novia.api.gui.component.provided.NVLabelButton;
import xyz.lumialights.novia.api.gui.geometry.Alignment;
import xyz.lumialights.novia.api.gui.component.provided.NVLabel;
import xyz.lumialights.novia.api.core.serialisation.Value;

import java.util.*;



//**********************************************************************************************************************
public class EditPanel
    extends StatefulGuiComponent
{
    //******************************************************************************************************************
    private record Content(@NotNull IStatefulComponent content) {}
    
    private record StackFrame(@NotNull Text title, @NotNull Content content) {}
    
    //******************************************************************************************************************
    private final NVLabel                 titleLabel;
    private final NVLabelButton           backButton;
    private final ConfigScreenLookAndFeel lookAndFeel;
    private final Deque<StackFrame>       contentStack = new LinkedList<>();
    
    private Content    content    = null;
    private PropertyId propertyId = null;
    
    //******************************************************************************************************************
    public EditPanel(final @NotNull ConfigScreenLookAndFeel lookAndFeel)
    {
        super(ScreenTexts.EMPTY);
        
        this.lookAndFeel = lookAndFeel;
        
        this.titleLabel = new NVLabel(ScreenTexts.EMPTY);
        this.titleLabel.textAlign.set(Alignment.MIDDLE_CENTRE);
        this.titleLabel.setColour(NVLabel.COLOUR_TEXT, 0xFF333333);
        
        this.backButton = new NVLabelButton(GuiApiLang.GUI_GO_BACK_BUTTON);
        this.backButton.setColour(NVLabel.COLOUR_TEXT, 0xFF333333);
        this.backButton.clicked.subscribe((sender, e) -> this.popFrame());
    }
    
    //==================================================================================================================
    @Override public @NotNull Value getValue() { return this.content.content().getValue(); }
    
    //==================================================================================================================
    @Override
    public void setValue(final @NotNull Value value)
    {
        if (!value.isList() && !value.isMap())
        {
            throw new IllegalArgumentException("Value is not a list or a Map");
        }
        
        this.content.content.setValue(value);
    }
    
    public void setProperty(final @NotNull PropertyId propertyId, final @NotNull Text title,
                            final @NotNull IStatefulComponent content)
    {
        this.propertyId = propertyId;
        this.pushFrame(title, content);
        this.setVisible(true);
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private void pushFrame(final @NotNull Text title, final @NotNull IStatefulComponent content)
    {
        this.contentStack.addLast(new StackFrame(this.titleLabel.getMessage(), this.content));
        this.updateContent(title, content);
    }
    
    private void popFrame()
    {
        if (this.contentStack.size() < 2)
        {
            this.setVisible(false);
            return;
        }
        
        final StackFrame frame = this.contentStack.pollLast();
        this.updateContent(frame.title, frame.content.content);
    }
    
    //==================================================================================================================
    @Override
    public void resized()
    {
        this.lookAndFeel.resize(this.getBounds());
        this.titleLabel.setBounds(this.lookAndFeel.getTitleBounds(
            this.getMessage(),
            this.lookAndFeel.getEditPanelClientBounds()));
        
        if (this.content != null)
        {
            ((GuiComponent) this.content.content).setBounds(this.lookAndFeel.getEditPanelListBounds());
        }
    }
    
    //==================================================================================================================
    @Override public void draw(final @NotNull Canvas canvas) { this.lookAndFeel.drawEditPanelBackground(canvas); }
    
    //==================================================================================================================
    private void updateContent(final @NotNull Text title, final @NotNull IStatefulComponent content)
    {
        this.removeChild((GuiComponent) this.content.content);
        
        this.content = new Content(content);
        this.titleLabel.setMessage(title);
        
        this.addChild((GuiComponent) this.content.content);
        this.resized();
    }
    
    //==================================================================================================================
    private void reset()
    {
        this.removeAllChildren();
        this.contentStack.clear();
        this.content    = null;
        this.propertyId = null;
    }
}
