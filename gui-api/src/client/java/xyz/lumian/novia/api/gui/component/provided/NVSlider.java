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

import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import xyz.lumian.novia.api.core.serialisation.Value;
import xyz.lumian.novia.api.core.util.NormalisedRange;
import xyz.lumian.novia.api.gui.GuiApiLang;
import xyz.lumian.novia.api.gui.GuiApiId;
import xyz.lumian.novia.api.gui.canvas.Canvas;
import xyz.lumian.novia.api.gui.canvas.ColourId;
import xyz.lumian.novia.api.gui.canvas.IGuiTemplate;
import xyz.lumian.novia.api.gui.component.IComponentNavigator;
import xyz.lumian.novia.api.gui.component.StatefulGuiComponent;
import xyz.lumian.novia.api.gui.component.input.KeyEvent;
import xyz.lumian.novia.api.gui.component.input.MouseEvent;
import xyz.lumian.novia.api.gui.geometry.Rectangle;
import xyz.lumian.novia.api.gui.property.GuiProperty;
import xyz.lumian.novia.api.gui.component.ComponentUtil;
import xyz.lumian.novia.api.gui.property.GuiPropertyBuilder;
import net.minecraft.client.gui.components.AbstractSliderButton;

import java.text.DecimalFormat;
import java.util.stream.Stream;



//**********************************************************************************************************************
/// A slider implementation modelled after Minecraft's [AbstractSliderButton], adjusted to the Novia GUI system.
///
/// A slider is a component, which can be used to gradually interpolate a number range, where the value will be between
/// the given minimum and maximum values (both inclusive).
///
/// This is a stateful GUI component, the value it contains represents the value of the slider,
/// it can be converted between number qualified [Value] objects.
public class NVSlider
    extends StatefulGuiComponent
{
    //******************************************************************************************************************
    public interface Template
    {
        //**************************************************************************************************************
        /// Draws the slider's background.
        /// @param canvas The [Canvas]
        /// @param slider The [NVSlider]
        void nvSliderDrawBackground(Canvas canvas, NVSlider slider);
        
        /// Draws the slider's thumb.
        /// @param canvas      The [Canvas]
        /// @param slider      The [NVSlider]
        /// @param thumbBounds The bounds of the thumb
        void nvSliderDrawThumb(Canvas canvas, NVSlider slider, Rectangle thumbBounds);
        
        /// Draws the slider's text.
        /// @param canvas The [Canvas]
        /// @param slider The [NVSlider]
        /// @param text   The text
        void nvSliderDrawText(Canvas canvas, NVSlider slider, Component text);
    }
    
    @FunctionalInterface
    public interface TextProvider
    {
        //**************************************************************************************************************
        Component provide(NVSlider slider);
    }
    
    //******************************************************************************************************************
    /// The colour of the slider text when the component is active.
    public static final ColourId COLOUR_TEXT = ColourId.reserve();
    
    /// The colour of the slider text when the component is inactive.
    public static final ColourId COLOUR_TEXT_INACTIVE = ColourId.reserve();
    
    //==================================================================================================================
    /// See [NVSlider#displayTextProvider].
    public static final TextProvider DEFAULT_DISPLAY_TEXT_PROVIDER;
    
    /// Ranges from 0 to 100 with a step size of `1.0`.
    /// @see NVSlider#range
    public static final NormalisedRange DEFAULT_RANGE;
    
    //==================================================================================================================
    /// The textures used for the slider's background (unfocused).
    public static final Identifier TEXTURE;
    
    /// The textures used for the slider's background (focused).
    public static final Identifier TEXTURE_HIGHLIGHTED;
    
    /// The textures used for the slider's handle (unfocused).
    public static final Identifier TEXTURE_HANDLE;
    
    /// The textures used for the slider's handle (focused).
    public static final Identifier TEXTURE_HANDLE_HIGHLIGHT;
    
    //==================================================================================================================
    static
    {
        TEXTURE                  = Identifier.withDefaultNamespace("widget/slider");
        TEXTURE_HIGHLIGHTED      = Identifier.withDefaultNamespace("widget/slider_highlighted");
        TEXTURE_HANDLE           = Identifier.withDefaultNamespace("widget/slider_handle");
        TEXTURE_HANDLE_HIGHLIGHT = Identifier.withDefaultNamespace("widget/slider_handle_highlighted");
        
        
        {
            final DecimalFormat format = new DecimalFormat("0");
            format.setMaximumFractionDigits(300);
            
            DEFAULT_DISPLAY_TEXT_PROVIDER = (slider -> GuiApiLang
                .GUI_COMPONENT_SLIDER_MESSAGE
                .withArgs(format.format(slider.getValueAsDouble())));
        }
        
        DEFAULT_RANGE = new NormalisedRange(0, 100);
    }
    
    //******************************************************************************************************************
    /// Describes the range the slider can slide between.
    /// @see NormalisedRange
    public final GuiProperty.NonNull<NormalisedRange> range;
    
    /// The text provider that provides the text to be displayed on the slider,
    /// where the given parameter is the value currently held by the slider.
    public final GuiProperty<TextProvider> displayTextProvider;
    
    //------------------------------------------------------------------------------------------------------------------
    private final Rectangle thumbBounds = new Rectangle();
    
    private double    normalised;
    private double    value;
    private Component text = CommonComponents.EMPTY;
    
    //******************************************************************************************************************
    /// Constructs a new slider with the given initial value.
    /// @param value   The initial slider value (will be clamped to the slider range)
    /// @param message The component message
    public NVSlider(final Number value, final Component message)
    {
        super(message);
        
        this.range               = GuiPropertyBuilder.nonNull(NVSlider.DEFAULT_RANGE)
            .withSetter(this::updateRange)
            .build();
        this.displayTextProvider = GuiPropertyBuilder.nonNull(NVSlider.DEFAULT_DISPLAY_TEXT_PROVIDER)
            .withNoArgSetter(this::updateText)
            .build();
        
        this.value      = this.range.get().clamp(value.doubleValue());
        this.normalised = this.range.get().normalise(this.value);
        
        this.setWantsFocus(true);
        this.updateText();
    }
    
    /// Constructs a new slider with the given initial value.
    /// @param value The initial slider value (will be clamped to the slider range)
    public NVSlider(final Number value) { this(value, CommonComponents.EMPTY); }
    
    /// Constructs a new slider with value [NormalisedRange#min()] of [NVSlider#DEFAULT_RANGE].
    public NVSlider() { this(NVSlider.DEFAULT_RANGE.min(), CommonComponents.EMPTY); }
    
    //==================================================================================================================
    /// Gets the current slider value as a number qualified [Value] object (see [#getValueAsDouble()]).
    /// @return The current slider [Value]
    @Override public Value getValue() { return new Value(this.value); }
    
    /// Gets the slider's current value as double. The number returned is any valid value that complies with the given
    /// range [#range].
    /// @return The slider value
    public double getValueAsDouble() { return this.value; }
    
    /// Gets the slider's value in normalised range between 0 (including) and 1 (including), where 0 is the start of the
    /// slider and 1 is the end.
    /// @return The normalised slider value
    public float getValueNormalised() { return this.range.get().normalise(this.value); }
    
    @Override public @Nullable IComponentNavigator getNavigator() { return null; }
    
    @Override
    public MutableComponent getNarrationMessage()
    {
        return Component.translatable("gui.narrate.slider", this.getMessage());
    }
    
    @Override
    public Stream<GuiPropertyDescription<?>> getGuiProperties()
    {
        return Stream.of(
            new GuiPropertyDescription<>(
                GuiApiId.GuiProperty.SLIDER_RANGE,
                this.range,
                NormalisedRange.CODEC),
            new GuiPropertyDescription<>(
                GuiApiId.GuiProperty.SLIDER_TEXT_PROVIDER,
                this.displayTextProvider));
    }
    
    //------------------------------------------------------------------------------------------------------------------
    /// Track start X pos.
    protected int getTrackStart() { return 0; }
    
    /// Track end X pos.
    protected int getTrackEnd() { return this.getWidth(); }
    
    /// Track length.
    protected int getTrackLength() { return (this.getTrackEnd() - this.getTrackStart()); }
    
    /// Thumb width.
    protected int getThumbSize() { return 8; }
    
    //==================================================================================================================
    /// Sets the value of this slider as a number qualified [Value] object. If the value is not a number value,
    /// this does nothing and if the value is outside the specified range, it will be clamped to fit.
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
    
    /// Sets the value of this slider to the new value. If the value is outside the range, it will be clamped to fit.
    /// @param value The new value to set
    public void setValue(final Number value)
    {
        if (this.setValueInternal(value.doubleValue()))
        {
            this.sendChangeNotification();
        }
    }
    
    /// Sets the slider's value in normalised range (see [#getValueNormalised()]).
    /// @param normalisedValue The normalised value
    public void setValueNormalised(final float normalisedValue) { this.setValueNormalisedInternal(normalisedValue); }
    
    //------------------------------------------------------------------------------------------------------------------
    private boolean setValueNormalisedInternal(final float normalised)
    {
        if (this.normalised != normalised)
        {
            this.normalised = Math.clamp(normalised, 0.0f, 1.0f);
            
            final NormalisedRange range = this.range.get();
            final double          old   = this.value;
            
            this.value = range.snap(range.denormalise(normalised));
            
            if (this.value != old)
            {
                this.updateThumbPos();
                this.updateText();
                
                return true;
            }
        }
        
        return false;
    }
    
    private boolean setValueInternal(final double value)
    {
        final NormalisedRange range   = this.range.get();
        final double          snapped = range.snap(value);
        
        if (this.value != snapped)
        {
            this.value      = snapped;
            this.normalised = range.normalise(snapped);
            
            this.updateThumbPos();
            this.updateText();
            
            return true;
        }
        
        return false;
    }
    
    private boolean setValueFromMouse(final MouseEvent e)
    {
        final int thumb_size = this.getThumbSize();
        return this.setValueNormalisedInternal(
            (e.localMouseX() - (this.getTrackStart() + (thumb_size * .5f)))
            / (float) (this.getTrackLength() - thumb_size)
        );
    }
    
    @Override
    public void appendCustomNarrations(final NarrationElementOutput builder)
    {
        builder.add(
            NarratedElementType.USAGE,
            Component.translatable("narration.slider.usage." + (this.isFocused() ? "focused" : "hovered")));
    }
    
    //==================================================================================================================
    @Override
    public void resized()
    {
        this.thumbBounds.setBounds(this
            .getLocalBounds()
            .setWidth(this.getThumbSize()));
        this.updateThumbPos();
    }
    
    //==================================================================================================================
    @Override
    public boolean onMouseDown(final MouseEvent e)
    {
        if (!this.isActive())
        {
            return false;
        }
        
        this.setValueFromMouse(e);
        return true;
    }
    
    @Override
    public boolean onMouseUp(final MouseEvent e)
    {
        if (!this.isActive())
        {
            return false;
        }
        
        ComponentUtil.playClickSound();
        this.sendChangeNotification();
        
        return true;
    }
    
    @Override
    public boolean onMouseDrag(final MouseEvent e)
    {
        if (!this.isActive())
        {
            return false;
        }
        
        this.setValueFromMouse(e);
        return true;
    }
    
    @Override
    public boolean onKeyDown(final KeyEvent e)
    {
        if (!this.isActive())
        {
            return false;
        }
        
        final boolean is_left_arrow = (e.input == GLFW.GLFW_KEY_LEFT);
        
        if (is_left_arrow || e.input == GLFW.GLFW_KEY_RIGHT)
        {
            this.setValueInternal(this.value + (this.range.get().step() * (is_left_arrow ? -1.0 : 1.0)));
            return true;
        }
        
        return false;
    }
    
    //==================================================================================================================
    @Override
    public void draw(final Canvas canvas)
    {
        final IGuiTemplate template = canvas.getTemplate();
        template.nvSliderDrawBackground(canvas, this);
        template.nvSliderDrawThumb     (canvas, this, new Rectangle(this.thumbBounds));
        template.nvSliderDrawText      (canvas, this, this.text);
    }
    
    //==================================================================================================================
    private void updateRange(final NormalisedRange range)
    {
        if (!range.isInRange(this.value))
        {
            this.setValue(range.clamp(this.value));
        }
        else
        {
            this.updateThumbPos();
        }
    }
    
    private void updateThumbPos()
    {
        final int length = (this.getTrackEnd() - this.getThumbSize());
        this.thumbBounds.setX(this.getTrackStart() + (int) (this.normalised * length));
    }
    
    private void updateText()
    {
        this.displayTextProvider
            .ifSet  (prov -> this.text = prov.provide(this))
            .ifEmpty(()   -> this.text = Component.empty());
    }
}
