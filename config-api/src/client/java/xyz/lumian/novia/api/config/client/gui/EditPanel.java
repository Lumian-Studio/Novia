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
package xyz.lumian.novia.api.config.client.gui;

import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import xyz.lumian.novia.api.config.PropertyId;
import xyz.lumian.novia.api.core.serialisation.Value;
import xyz.lumian.novia.api.gui.GuiApiLang;
import xyz.lumian.novia.api.gui.canvas.Canvas;
import xyz.lumian.novia.api.gui.component.GuiComponent;
import xyz.lumian.novia.api.gui.component.StatefulGuiComponent;
import xyz.lumian.novia.api.gui.component.integration.IStatefulComponent;
import xyz.lumian.novia.api.gui.component.provided.NVLabel;
import xyz.lumian.novia.api.gui.component.provided.NVLabelButton;
import xyz.lumian.novia.api.gui.geometry.Alignment;

import java.util.*;



//**********************************************************************************************************************
public class EditPanel
    extends StatefulGuiComponent
{
    //******************************************************************************************************************
    private record Content(IStatefulComponent content) {}
    
    private record StackFrame(Component title, Content content) {}
    
    //******************************************************************************************************************
    private final NVLabel                 titleLabel;
    private final NVLabelButton           backButton;
    private final ConfigScreenLookAndFeel lookAndFeel;
    private final Deque<StackFrame>       contentStack = new LinkedList<>();
    
    @Nullable
    private Content content = null;
    
    @Nullable
    private PropertyId propertyId = null;
    
    //******************************************************************************************************************
    public EditPanel(final ConfigScreenLookAndFeel lookAndFeel)
    {
        super(CommonComponents.EMPTY);
        
        this.lookAndFeel = lookAndFeel;
        
        this.titleLabel = new NVLabel(CommonComponents.EMPTY);
        this.titleLabel.textAlign.set(Alignment.MIDDLE_CENTRE);
        this.titleLabel.setColour(NVLabel.COLOUR_TEXT, 0xFF333333);
        
        this.backButton = new NVLabelButton(GuiApiLang.GUI_GO_BACK_BUTTON);
        this.backButton.setColour(NVLabel.COLOUR_TEXT, 0xFF333333);
        this.backButton.clicked.subscribe((sender, e) -> this.popFrame());
    }
    
    //==================================================================================================================
    @Override public Value getValue() { return Objects.requireNonNull(this.content).content().getValue(); }
    
    //==================================================================================================================
    @Override
    public void setValue(final Value value)
    {
        if (!value.isList() && !value.isMap())
        {
            throw new IllegalArgumentException("Value is not a list or a Map");
        }
        
        Objects.requireNonNull(this.content).content.setValue(value);
    }
    
    public void setProperty(final PropertyId propertyId, final Component title, final IStatefulComponent content)
    {
        this.propertyId = propertyId;
        this.pushFrame(title, content);
        this.setVisible(true);
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private void pushFrame(final Component title, final IStatefulComponent content)
    {
        this.contentStack.addLast(new StackFrame(this.titleLabel.getMessage(), Objects.requireNonNull(this.content)));
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
    @Override public void draw(final Canvas canvas) { this.lookAndFeel.drawEditPanelBackground(canvas); }
    
    //==================================================================================================================
    private void updateContent(final Component title, final IStatefulComponent content)
    {
        if (this.content != null)
        {
            this.removeChild((GuiComponent) this.content.content);
        }
        
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
