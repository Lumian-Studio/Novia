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

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import org.apache.commons.lang3.math.NumberUtils;
import org.lwjgl.glfw.GLFW;
import xyz.lumian.novia.api.core.serialisation.Value;
import xyz.lumian.novia.api.core.util.NormalisedRange;
import xyz.lumian.novia.api.gui.GuiApiId;
import xyz.lumian.novia.api.gui.canvas.Canvas;
import xyz.lumian.novia.api.gui.canvas.ColourId;
import xyz.lumian.novia.api.gui.component.StatefulGuiComponent;
import xyz.lumian.novia.api.gui.geometry.Alignment;
import xyz.lumian.novia.api.gui.geometry.Point;
import xyz.lumian.novia.api.gui.geometry.Rectangle;
import xyz.lumian.novia.api.gui.component.input.KeyEvent;
import xyz.lumian.novia.api.gui.component.input.MouseEvent;
import xyz.lumian.novia.api.gui.property.GuiProperty;
import xyz.lumian.novia.api.gui.property.GuiPropertyBuilder;

import java.text.DecimalFormat;
import java.util.function.Supplier;
import java.util.stream.Stream;



//**********************************************************************************************************************
/// An extension to [NVTextBox] which can be used to display and edit numeric values specifically.
///
/// This special text box comes with two [NVSimpleButton], used to decrement and increment the current numeric
/// value and a [NVTextBox] that displays the current value.
///
/// This is a stateful GUI component, the number it contains represents the number currently contained inside the box,
/// it can be converted between number qualified [Value] objects.
public class NVNumericBox
    extends StatefulGuiComponent
{
    //******************************************************************************************************************
    public interface Template
    {
        //**************************************************************************************************************
        /// Draws the numeric box's up- and down-arrow button background.
        /// @param canvas     The [Canvas]
        /// @param numericBox The [NVNumericBox]
        /// @param button     The button to draw
        /// @param isUpButton Draws the up-arrow button if `true`, otherwise draws the down-arrow button
        void nvNumericBoxDrawArrowButton(Canvas canvas, NVNumericBox numericBox, NVAbstractButton button,
                                         boolean isUpButton);
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private class ArrowButton
        extends NVAbstractButton
    {
        //**************************************************************************************************************
        private final boolean isUp;
        
        //**************************************************************************************************************
        public ArrowButton(final boolean isUp)
        {
            this.isUp = isUp;
            this.setWantsFocus(false);
            this.setPinned(true);
        }
        
        //==============================================================================================================
        @Override
        public void draw(final Canvas canvas)
        {
            canvas.getTemplate().nvNumericBoxDrawArrowButton(canvas, NVNumericBox.this, this, this.isUp);
        }
    }
    
    //******************************************************************************************************************
    public static final ColourId COLOUR_ARROW          = ColourId.reserve();
    public static final ColourId COLOUR_ARROW_INACTIVE = ColourId.reserve();
    
    //==================================================================================================================
    /// See [NVNumericBox#range].
    public static final NormalisedRange DEFAULT_RANGE = NormalisedRange.FULL_RANGE;
    
    /// See [NVNumericBox#numberFormat].
    public static final Supplier<DecimalFormat> DEFAULT_FORMAT_SUPPLIER = (() ->
    {
        final DecimalFormat format = new DecimalFormat("0");
        format.setMaximumFractionDigits(300);
        
        return format;
    });
    
    //------------------------------------------------------------------------------------------------------------------
    private static final int COUNT_START_DELAY_MS = 500;
    private static final int COUNT_APPLY_DELAY_MS = 100;
    
    //******************************************************************************************************************
    /// Describes the range the box can step between.
    /// @see NormalisedRange
    public final GuiProperty.NonNull<NormalisedRange> range;
    
    /// Describes the formatting of the number as text in the text box.
    /// @see DecimalFormat
    public final GuiProperty.NonNull<DecimalFormat> numberFormat;
    
    //==================================================================================================================
    private final NVTextBox   textBox;
    private final ArrowButton upButton;
    private final ArrowButton downButton;
    
    private int    countMode = 0;
    private long   startMs   = 0;
    private long   applyMs   = 0;
    private double value;
    
    //******************************************************************************************************************
    /// Constructs a new numeric text box.
    /// @param value   The initial value for the numeric box
    /// @param message The component message
    public NVNumericBox(final double value, final Component message)
    {
        super(message);
        
        this.range        = GuiPropertyBuilder.nonNull(NVNumericBox.DEFAULT_RANGE)
            .withNoArgSetter(this::updateRange)
            .build();
        this.numberFormat = GuiPropertyBuilder.nonNull(NVNumericBox.DEFAULT_FORMAT_SUPPLIER.get())
            .withSetter(this::updateText)
            .build();
        
        this.upButton = this.addChild(new ArrowButton(true));
        this.upButton.clicked.subscribe((_, _) -> this.countUp());
        
        this.downButton = this.addChild(new ArrowButton(false));
        this.downButton.clicked.subscribe((_, _) -> this.countDown());

        this.value = this.range.get().clamp(value);
        
        this.textBox = this.addChild(new NVTextBox());
        this.textBox.predicateStopsInput.set(true);
        this.textBox.textPredicate.set(NumberUtils::isParsable);
        this.textBox.valueChanged.subscribe((sender, _) -> this.textboxTextChanged((NVTextBox) sender));
        
        this.setMonitorChildren(true);
        this.setWantsFocus(false);
        this.updateText(this.numberFormat.get());
    }
    
    /// Constructs a new numeric text box with an empty message.
    /// @param value The initial value for the numeric box
    public NVNumericBox(final double value) { this(value, CommonComponents.EMPTY); }
    
    /// Constructs a new numeric text box with an initial value of 0.
    public NVNumericBox() { this(0, CommonComponents.EMPTY); }
    
    //==================================================================================================================
    /// Gets the last valid number of this text box as a [Value] object.
    /// @return The number [Value]
    @Override public Value getValue() { return new Value(this.value); }
    
    /// Gets the last valid number of this number box.
    /// @return The number
    public double getValueAsDouble() { return this.value; }
    
    /// Gets the textual representation of the number inside the numeric text box.
    /// @return The value string
    public String getText() { return this.textBox.getText(); }
    
    /// Gets the internal text box, this should only be used for styling and templating the text box.
    /// @return The [NVTextBox]
    public NVTextBox getTextBox() { return this.textBox; }
    
    @Override
    public Stream<GuiPropertyDescription<?>> getGuiProperties()
    {
        return Stream.of(
            new GuiPropertyDescription<>(
                GuiApiId.GuiProperty.NUMERIC_BOX_RANGE,
                this.range,
                NormalisedRange.CODEC),
            new GuiPropertyDescription<>(
                GuiApiId.GuiProperty.NUMERIC_BOX_FORMAT,
                this.numberFormat,
                Codec.STRING.xmap(DecimalFormat::new, DecimalFormat::toPattern))
        );
    }
    
    //==================================================================================================================
    /// Sets the value of this numeric box as a number qualified [Value] object. If the value is not a number
    /// value, this does nothing and if the value is outside the specified range, it will be clamped to fit.
    /// @param value The new boolean [Value]
    @Override
    public void setValue(final Value value)
    {
        if (!value.isNumber())
        {
            return;
        }
        
        this.setValue(value.getNumber().doubleValue());
    }
    
    /// Sets the value of this box to the new value. If the value is outside the range, it will be clamped to fit.
    /// @param value The new value to set
    public void setValue(final Number value)
    {
        final double new_value = this.range.get().clamp(value.doubleValue());
        
        if (this.value != new_value)
        {
            this.value = new_value;
            
            this.updateText(this.numberFormat.get());
            this.sendChangeNotification();
        }
    }
    
    /// Sets numeric box's number text explicitly, if it is not a valid number nothing happens.
    /// @param text The number text to set
    public void setText(final String text) { this.textBox.setText(text); }
    
    //==================================================================================================================
    private void countUp  () { this.setValue(this.value + this.range.get().step()); }
    private void countDown() { this.setValue(this.value - this.range.get().step()); }
    
    //==================================================================================================================
    @Override
    public void resized()
    {
        final int       size        = (int) (this.getHeight() * 0.5f);
        final Rectangle local       = this.getLocalBounds();
        final Rectangle button_rect = local
            .withWidth(size)
            .align(Alignment.MIDDLE_RIGHT, local);
        
        this.upButton  .setBounds(button_rect.removeTop(size));
        this.downButton.setBounds(button_rect);
        this.textBox   .setBounds(local);
    }
    
    //==================================================================================================================
    @Override
    public boolean onMouseScroll(final MouseEvent e)
    {
        if (!this.isFocused() || !this.isActive())
        {
            return false;
        }
        
        if (e.deltaY > 0)
        {
            this.countUp();
            return true;
        }
        else if (e.deltaY < 0)
        {
            this.countDown();
            return true;
        }
        
        return false;
    }
    
    @Override
    public boolean onMouseDown(final MouseEvent e)
    {
        if (!this.isActive())
        {
            return false;
        }
        
        this.countMode = 0;
        
        if (this.upButton.isDragging() || this.downButton.isDragging())
        {
            this.countMode = (this.upButton.isDragging() ? 1 : -1);
            this.startMs   = Util.getMillis();
            this.applyMs   = Util.getMillis();
            
            return true;
        }
        
        return super.onMouseDown(e);
    }
    
    @Override
    public boolean onMouseUp(final MouseEvent e)
    {
        e.enableCursor();
        this.countMode = 0;
        
        return super.onMouseUp(e);
    }
    
    @Override
    public boolean onMouseDrag(final MouseEvent e)
    {
        if (!this.isActive())
        {
            return false;
        }
        
        if (this.countMode != 0)
        {
            this.countMode = 0;
            e.disableCursor();
        }
        
        if (this.upButton.isDragging() || this.downButton.isDragging())
        {
            if      (e.deltaY < 0) this.countUp();
            else if (e.deltaY > 0) this.countDown();
            
            return true;
        }
        
        return super.onMouseDrag(e);
    }
    
    @Override
    public boolean onKeyDown(final KeyEvent e)
    {
        if (!this.isActive())
        {
            return false;
        }
        
        if (e.hasControlDown())
        {
            switch (e.input)
            {
                case GLFW.GLFW_KEY_DOWN:
                    this.countDown();
                    return true;
                
                case GLFW.GLFW_KEY_UP:
                    this.countUp();
                    return true;
            }
        }
        
        return super.onKeyDown(e);
    }
    
    //==================================================================================================================
    protected void textboxTextChanged(final NVTextBox box)
    {
        this.value = NumberUtils.toDouble(box.getText());
        this.sendChangeNotification();
    }
    
    //==================================================================================================================
    @Override
    public void onDeltaTick(final Point mousePos, final float delta)
    {
        if (this.countMode == 0)
        {
            return;
        }
        
        final long now = Util.getMillis();
        
        if (
            (now - this.startMs) > NVNumericBox.COUNT_START_DELAY_MS
            && (now - this.applyMs) > NVNumericBox.COUNT_APPLY_DELAY_MS
        )
        {
            if (this.countMode > 0) this.countUp();
            else                    this.countDown();
            
            this.applyMs = now;
        }
    }
    
    //==================================================================================================================
    private void updateRange() { this.setValue(this.value); }
    
    private void updateText(final DecimalFormat format)
    {
        this.textBox.mute();
        this.textBox.setText(format.format(this.value));
        this.textBox.unmute();
    }
}
