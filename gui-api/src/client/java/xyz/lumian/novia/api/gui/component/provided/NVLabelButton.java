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
package xyz.lumian.novia.api.gui.component.provided;

import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import xyz.lumian.novia.api.gui.geometry.Alignment;



//**********************************************************************************************************************
/// An [NVAbstractButton] implementation with an optional label and icon.
public class NVLabelButton
    extends NVAbstractButton
{
    //******************************************************************************************************************
    private final NVLabel label;

    //******************************************************************************************************************
    /// Constructs a new button with the given action and label.
    /// @param label   The text drawn onto the button
    /// @param message The message and the initial text on the button
    public NVLabelButton(final Component label, final Component message)
    {
        super(message);

        this.label = this.addChild(new NVLabel(label));
        this.label.textAlign.set(Alignment.CENTRE);
    }
    
    /// Constructs a new button with the given label.
    /// @param label The text drawn onto the button
    public NVLabelButton(final Component label) { this(label, CommonComponents.EMPTY); }
    
    /// Constructs a new button with no text.
    public NVLabelButton() { this(CommonComponents.EMPTY); }
    
    //==================================================================================================================
    /// Gets the internal label the button uses to display its text. Use this only for styling purposes or changing
    /// text.
    /// @return The internal [NVLabel]
    public NVLabel getLabel() { return this.label; }
    
    //==================================================================================================================
    @Override public void resized() { this.label.setBounds(this.getLocalBounds()); }
}
