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

import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;
import xyz.lumian.novia.api.gui.GuiApiId;
import xyz.lumian.novia.api.gui.canvas.Canvas;
import xyz.lumian.novia.api.gui.canvas.ColourId;
import xyz.lumian.novia.api.gui.canvas.IGuiTemplate;
import xyz.lumian.novia.api.gui.component.GuiComponent;
import xyz.lumian.novia.api.gui.component.IComponentNavigator;
import xyz.lumian.novia.api.gui.event.GuiEvent;
import xyz.lumian.novia.api.gui.font.GuiFont;
import xyz.lumian.novia.api.gui.font.TextLayout;
import xyz.lumian.novia.api.gui.geometry.Alignment;
import xyz.lumian.novia.api.gui.property.GuiProperty;
import xyz.lumian.novia.api.gui.property.GuiPropertyBuilder;

import java.util.*;
import java.util.stream.Stream;



//**********************************************************************************************************************
/// A component, which renders an associated text on the screen. The advantage of using a component over directly
/// drawing the text to the screen is the ease of positioning and transformation.
public class NVLabel
    extends GuiComponent
{
    //******************************************************************************************************************
    public interface Template
    {
        //**************************************************************************************************************
        /// Draws the label's background.
        /// @param canvas The [Canvas]
        /// @param label  The [NVLabel]
        void nvLabelDrawBackground(Canvas canvas, NVLabel label);

        /// Draws the label's text.
        /// @param canvas The [Canvas]
        /// @param label  The [NVLabel]
        /// @param text   The text to draw
        void nvLabelDrawText(Canvas canvas, NVLabel label, FormattedCharSequence text);
    }

    /// Describes how the text inside the label should be trimmed if it is too long to fit.
    @FunctionalInterface
    public interface TrimFunction
    {
        /// Trims the given text.
        /// @param text  The text to trim
        /// @param font  The [GuiFont] used to determine the size of the text on screen
        /// @param label The [NVLabel] component
        /// @return The trimmed text
        FormattedCharSequence trim(Component text, GuiFont font, NVLabel label);
    }
    
    //******************************************************************************************************************
    /// The colour used for the text when the component is active.
    public static final ColourId COLOUR_TEXT = ColourId.reserve();

    /// The colour used for the text when the component is inactive.
    public static final ColourId COLOUR_TEXT_INACTIVE = ColourId.reserve();
    
    //==================================================================================================================
    /// See [NVLabel#textAlign].
    public static final Alignment DEFAULT_ALIGNMENT = Alignment.LEFT;
    
    /// See [NVLabel#trimFunction].
    public static final TrimFunction DEFAULT_TRIM_FUNCTION;

    //==================================================================================================================
    /// A [TrimFunction] that does no trimming and just converts the [Component] to [FormattedCharSequence].
    public static final TrimFunction NO_TRIM_FUNCTION;
    
    //==================================================================================================================
    static
    {
        DEFAULT_TRIM_FUNCTION = ((text, font, label) ->
        {
            final int  max_width = (label.getWidth() - TextLayout.getTextWidthFitted(font, CommonComponents.ELLIPSIS));
            final Component text1     = TextLayout.trimToWidth(font, text, max_width);
            return Language.getInstance().getVisualOrder(FormattedText.composite(text1, CommonComponents.ELLIPSIS));
        });

        NO_TRIM_FUNCTION = ((text, _, _) -> text.getVisualOrderText());
    }
    
    //******************************************************************************************************************
    /// Describes the alignment of the text inside the label's bounds.
    public final GuiProperty.NonNull<Alignment> textAlign;
    
    /// Describes the function to be used to trim the text if it is too long, and how this should happen. If the text
    /// should not be trimmed [NVLabel#NO_TRIM_FUNCTION] can be used instead.
    public final GuiProperty.NonNull<TrimFunction> trimFunction;
    
    //==================================================================================================================
    /// Triggered whenever the component's text changed.
    public final GuiEvent.Simple textChanged = new GuiEvent.Simple();
    
    //==================================================================================================================
    private Component             text    = CommonComponents.EMPTY;
    private FormattedCharSequence trimmed = FormattedCharSequence.EMPTY;
    
    //******************************************************************************************************************
    /// Constructs a new label component with the given text and message.
    /// @param text    The text to draw on the label
    /// @param message The options of this label
    public NVLabel(final Component text, final Component message)
    {
        super(message);
        
        this.textAlign    = GuiProperty.nonNull(NVLabel.DEFAULT_ALIGNMENT);
        this.trimFunction = GuiPropertyBuilder.nonNull(NVLabel.DEFAULT_TRIM_FUNCTION)
            .withSetter(_ -> this.updateText(this.getFont()))
            .build();
        
        this.setText(text);
    }
    
    /// Constructs a new label component with the given text as display text and message.
    /// @param text The text to draw on the label and the message of the component
    public NVLabel(final Component text) { this(text, text); }

    /// Constructs a new empty label component without display text and message.
    public NVLabel() { this(CommonComponents.EMPTY); }

    //==================================================================================================================
    @Override public @Nullable IComponentNavigator getNavigator() { return null; }

    /// {@return the text that is drawn onto this label}
    public Component getText() { return this.text; }
    
    //------------------------------------------------------------------------------------------------------------------
    @Override
    public Stream<GuiPropertyDescription<?>> getGuiProperties()
    {
        return Stream.of(
            new GuiPropertyDescription<>(
                GuiApiId.GuiProperty.LABEL_TEXT_ALIGN,
                this.textAlign,
                Alignment.CODEC),
            new GuiPropertyDescription<>(
                GuiApiId.GuiProperty.LABEL_TRIM_FUNC,
                this.trimFunction));
    }
    
    //==================================================================================================================
    /// Gets whether the given Label's text is empty; a label is empty if it contains no characters or only whitespace.
    /// @return `true` if the label text is empty
    public boolean isEmpty()
    {
        return Objects
            .requireNonNullElseGet(this.text.tryCollapseToString(), this.text::getString)
            .trim()
            .isEmpty();
    }
    
    //==================================================================================================================
    /// Sets the text that should be drawn on the label
    /// @param text The new text
    public void setText(final Component text)
    {
        if (!Objects.equals(this.text, text))
        {
            this.text = Objects.requireNonNull(text, "text must not be null");
            this.updateText(this.getFont());
            
            this.onTextChanged();
            this.textChanged.post(this);
        }
    }
    
    //==================================================================================================================
    @Override public void resized() { this.updateText(this.getFont()); }

    //==================================================================================================================
    @Override
    public void onFontChanged(final @Nullable GuiFont font)
    {
        this.updateText(Objects.requireNonNullElseGet(font, this::getFont));
    }

    //==================================================================================================================
    @Override
    public void draw(final Canvas canvas)
    {
        final IGuiTemplate template = canvas.getTemplate();
        template.nvLabelDrawBackground(canvas, this);
        template.nvLabelDrawText(canvas, this, this.trimmed);
    }

    //==================================================================================================================
    public void onTextChanged() {}
    
    //==================================================================================================================
    private void updateText(final GuiFont font)
    {
        this.trimmed = (TextLayout.getTextWidth(font, this.text) > this.getWidth()
            ? this.trimFunction.get().trim(this.text, font, this)
            : this.text.getVisualOrderText());
    }
}
