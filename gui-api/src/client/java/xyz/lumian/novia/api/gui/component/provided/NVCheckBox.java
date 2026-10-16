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
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import xyz.lumian.novia.api.core.serialisation.Value;
import xyz.lumian.novia.api.gui.GuiApiId;
import xyz.lumian.novia.api.gui.canvas.Canvas;
import xyz.lumian.novia.api.gui.canvas.IGuiTemplate;
import xyz.lumian.novia.api.gui.component.ComponentUtil;
import xyz.lumian.novia.api.gui.component.IComponentNavigator;
import xyz.lumian.novia.api.gui.component.StatefulGuiComponent;
import xyz.lumian.novia.api.gui.component.input.KeyEvent;
import xyz.lumian.novia.api.gui.component.input.MouseEvent;

import java.util.Optional;


//**********************************************************************************************************************
/// A clickable box that can be checked or unchecked on input events; this is a boolean component that can have one of
/// two values: `true` or `false`.
///
/// This component listens to enter and space keyboard input events to toggle its current state if it is focused.
///
/// This is a stateful GUI component, the value it contains represents the checked state, it can be converted between
/// boolean qualified [Value] objects.
public class NVCheckBox
    extends StatefulGuiComponent
{
    //******************************************************************************************************************
    /// Describes the texture being drawn for the checkbox depending on its current state.
    /// @param activeChecked     The texture to draw when the checkbox is active and checked
    /// @param activeUnchecked   The texture to draw when the checkbox is active and unchecked
    /// @param focusedChecked    The texture to draw when the checkbox is active, focused and checked
    /// @param focusedUnchecked  The texture to draw when the checkbox is active, focused and unchecked
    /// @param inactiveChecked   The texture to draw when the checkbox is inactive and checked
    /// @param inactiveUnchecked The texture to draw when the checkbox is inactive and unchecked
    public record CheckboxTexture(
        Identifier activeChecked,
        Identifier activeUnchecked,
        Identifier focusedChecked,
        Identifier focusedUnchecked,
        Identifier inactiveChecked,
        Identifier inactiveUnchecked
    )
    {
        //**************************************************************************************************************
        /// Gets the texture to be drawn depending on the given state attributes.
        /// @param checked Whether the checkbox is checked
        /// @param focused Whether the checkbox is focused
        /// @param active  Whether the checkbox is active
        /// @return The texture [Identifier]
        public Identifier get(final boolean checked, final boolean focused, final boolean active)
        {
            if (active)
            {
                if (focused)
                {
                    return (checked ? this.focusedChecked : this.focusedUnchecked);
                }
                
                return (checked ? this.activeChecked : this.activeUnchecked);
            }
            
            return (checked ? this.inactiveChecked : this.inactiveUnchecked);
        }
    }
    
    public interface Template
    {
        //**************************************************************************************************************
        /// Draws the background of the checkbox.
        /// @param canvas   The [Canvas]
        /// @param checkBox The [NVCheckBox]
        void nvCheckBoxDrawBackground(Canvas canvas, NVCheckBox checkBox);

        /// Draws the check mark of the checkbox.
        /// @param canvas   The [Canvas]
        /// @param checkBox The [NVCheckBox]
        void nvCheckBoxDrawCheckMark(Canvas canvas, NVCheckBox checkBox);
    }
    
    //******************************************************************************************************************
    /// The textures used to render the checkbox.
    public static final CheckboxTexture TEXTURES = Optional.of(GuiApiId.of("widget"))
        .map(widgets -> new CheckboxTexture(
            widgets.withSuffix("/checkbox_checked"),
            widgets.withSuffix("/checkbox_unchecked"),
            widgets.withSuffix("/checkbox_checked_focused"),
            widgets.withSuffix("/checkbox_unchecked_focused"),
            widgets.withSuffix("/checkbox_inactive"),
            widgets.withSuffix("/checkbox_inactive")))
        .orElseThrow();
    
    //******************************************************************************************************************
    private boolean checked;
    
    //******************************************************************************************************************
    /// Constructs a new checkbox with the given initial state and component message.
    /// @param checked `true` if the checkbox should be checked, `false` otherwise
    /// @param message The component message
    public NVCheckBox(final boolean checked, final Component message)
    {
        super(message);
        
        this.checked = checked;
        this.setWantsFocus(true);
    }
    
    /// Constructs a new checkbox with the given initial state.
    /// @param checked `true` if the checkbox should be checked, `false` otherwise
    public NVCheckBox(final boolean checked) { this(checked, CommonComponents.EMPTY); }
    
    /// Constructs a new unchecked checkbox.
    public NVCheckBox() { this(false); }
    
    //==================================================================================================================
    /// Gets the state of this checkbox as a boolean qualified [Value].
    /// @return The boolean [Value]
    @Override public Value getValue() { return new Value(this.checked); }
    
    @Override public @Nullable IComponentNavigator getNavigator() { return null; }
    
    //==================================================================================================================
    /// Whether this checkbox is currently checked.
    /// @return `true` if it is checked
    public boolean isChecked() { return this.checked; }
    
    //==================================================================================================================
    /// Sets whether the checkbox should be checked, does nothing if it already has the given state.
    /// @param checked `true` if it should be checked
    public void setChecked(final boolean checked)
    {
        if (this.checked != checked)
        {
            this.checked = checked;
            this.sendChangeNotification();
        }
    }
    
    /// Toggles the state of the checkbox to the opposite of its current state.
    public void toggle() { this.setChecked(!this.checked); }
    
    /// Sets the value of this checkbox as a boolean qualified [Value] object. If the value is not a boolean value,
    /// this does nothing.
    /// @param value The new boolean [Value]
    @Override
    public void setValue(final Value value)
    {
        if (!value.isBoolean())
        {
            return;
        }
        
        this.setChecked(value.getBoolean());
    }
    
    //==================================================================================================================
    @Override
    public boolean onMouseDown(final MouseEvent e)
    {
        if (this.isActive())
        {
            this.toggle();
            ComponentUtil.playClickSound();
            
            return true;
        }
        
        return false;
    }
    
    @Override
    public boolean onKeyDown(final KeyEvent e)
    {
        if (this.isActive() && e.isSelection())
        {
            this.toggle();
            return true;
        }
        
        return false;
    }
    
    //==================================================================================================================
    @Override
    public void draw(final Canvas canvas)
    {
        final IGuiTemplate template = canvas.getTemplate();
        template.nvCheckBoxDrawBackground(canvas, this);
        template.nvCheckBoxDrawCheckMark (canvas, this);
    }
}
