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
package xyz.lumian.novia.api.testmod.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import xyz.lumian.novia.api.core.serialisation.Value;
import xyz.lumian.novia.api.core.util.Colour;
import xyz.lumian.novia.api.core.util.NormalisedRange;
import xyz.lumian.novia.api.gui.canvas.AffineTransform;
import xyz.lumian.novia.api.gui.canvas.Canvas;
import xyz.lumian.novia.api.gui.canvas.brush.gradient.Direction;
import xyz.lumian.novia.api.gui.canvas.brush.gradient.Gradient;
import xyz.lumian.novia.api.gui.component.GuiComponent;
import xyz.lumian.novia.api.gui.component.GuiScreen;
import xyz.lumian.novia.api.gui.component.provided.*;
import xyz.lumian.novia.api.gui.font.*;
import xyz.lumian.novia.api.gui.geometry.Alignment;
import xyz.lumian.novia.api.gui.geometry.Rectangle;

import java.time.Duration;
import java.util.*;



//**********************************************************************************************************************
// Inherit from GuiScreen instead of Screen
public class ComponentTestScreen
    extends GuiScreen
{
    //******************************************************************************************************************
    public static class Item
        implements INVItemModel
    {
        //**************************************************************************************************************
        public NVLabel title;
        
        //**************************************************************************************************************
        public Item(final String text)
        {
            this.title = new NVLabel(Component.literal(text));
            this.title.textAlign.set(Alignment.MIDDLE_LEFT);
        }
        
        //==============================================================================================================
        @Override public List<GuiComponent> getChildren() { return List.of(this.title); }
        
        @Override
        public void resized(final Rectangle bounds)
        {
            this.title.setBounds(bounds.withLocalPos().withPadding(5, 0));
        }
        
        //==============================================================================================================
        @Override
        public void draw(final Canvas canvas, final Rectangle bounds, final int index, final boolean selected,
                         final boolean hovered, final boolean focused)
        {
            final Rectangle local_bounds = bounds.withLocalPos();
            
            if (focused)
            {
                canvas.setColour(Colour.WHITE);
                canvas.drawRect(local_bounds);
            }
            
            if (selected || hovered)
            {
                canvas.setColour(Colour.WHITE.withAlpha(selected ? 0x66 : 0x33));
                canvas.fill(local_bounds);
            }
        }
    }
    
    //******************************************************************************************************************
    public NVSimpleButton  basicComponentsButton;
    public NVListBox<Item> testListBox;
    public NVSlider        testSlider;
    public NVDropdown      testDropdown;
    
    //******************************************************************************************************************
    // In the constructor we set up our child components and add them to the screen
    public ComponentTestScreen(final Component title)
    {
        super(title);

        // create and add component
        // note: we can also create the component and add it at the end
        this.basicComponentsButton = this.addChild(new NVSimpleButton(Component.literal("Basic Components")));
        this.basicComponentsButton.setTooltip(Tooltip.create(Component.literal("Add Listbox Item\nThis is a button haha")));
        this.basicComponentsButton.setTooltipDelay(Duration.ofSeconds(1));
        this.basicComponentsButton.clicked.subscribe((_, _) ->
        {
            this.testListBox.addItem(new Item(UUID.randomUUID().toString()));
            this.testListBox.refreshList();
        });
        
        this.testListBox = this.addChild(new NVListBox<>());
        this.testListBox.itemSize.set(20);
        this.testListBox.selectionMode.set(NVListBox.SelectionMode.MULTIPLE);
        this.testListBox.canFocusItems.set(true);
        this.testListBox.setTooltip(Tooltip.create(Component.literal("Test List Box")));
        this.testListBox.setTooltipDelay(Duration.ofSeconds(3));
        this.testListBox.addAllItems(List.of(
            new Item("Hello"),
            new Item("2"),
            new Item("2rfeswafewfef"),
            new Item("This is your mother speaking"),
            new Item("Can i has rice pls?"),
            new Item("Don't stop the bleedin', hold on to that feeeeeeelin'"),
            new Item("Hello 2")
        ));
        this.testListBox.refreshList();
        
        this.testSlider = this.addChild(new NVSlider());
        this.testSlider.range.set(new NormalisedRange(0, 100, 2));
        this.testSlider.displayTextProvider.set(val -> Component.literal("Value: " + val.getValueAsDouble()));
        this.testSlider.setTooltip(Tooltip.create(Component.literal("Slider value: 0%")));
        this.testSlider.valueChanged.subscribe((_, _) ->
        {
            final double normalised = this.testSlider.range.get().normalise(this.testSlider.getValueAsDouble());
            this.testSlider.setTooltip(
                Tooltip.create(Component.literal("Slider value: " + ((int)(normalised * 100.0) / 100.0) + "%")));
        });
        this.testSlider.setFont(GuiFont.UNICODE.get());
        
        this.testDropdown = this.addChild(new NVDropdown());
        this.testDropdown.addOption(NVDropdown.Option.forValue(
            this.testDropdown,
            new Value(323)));
        this.testDropdown.addOption(NVDropdown.Option.forValue(
            this.testDropdown,
            new Value(true)));
        this.testDropdown.addOption(NVDropdown.Option.forValue(
            this.testDropdown,
            new Value("this is some string lel")));
        this.testDropdown.addOption(NVDropdown.Option.forValue(
            this.testDropdown,
            new Value(Map.of(new Value("key"), new Value("value")))));
        this.testDropdown.addOption(NVDropdown.Option.forValue(
            this.testDropdown,
            new Value(List.of(new Value(1), new Value(2), new Value(3)))));
        this.testDropdown.addOption(this.testDropdown.new Option(
            "Map copy",
            new Value(Map.of(new Value("key"), new Value("value"))),
            Component.nullToEmpty("Map copy")));
        this.testDropdown.addOption(this.testDropdown.new Option(
            "List copy",
            new Value(List.of(new Value(1), new Value(2), new Value(3))),
            Component.nullToEmpty("List copy")));
        this.testDropdown.addOption(NVDropdown.Option.forValue(this.testDropdown, new Value()));
        this.testDropdown.addOption(NVDropdown.Option.forValue(this.testDropdown, new Value(434.0f)));

        // setting our gui default font to be shadowed
        this.setFont(this.getFont().withShadow(true));
    }

    //==================================================================================================================
    @Override
    public void resized()
    {
        final Rectangle bounds = this.getLocalBounds();
        
        this.basicComponentsButton.setBounds(5, 5, 100, 20);
        this.testListBox.setBounds(bounds
            .withLeft(this.basicComponentsButton.getRight() + 5)
            .withBottom(bounds.getCentreY()));
        this.testSlider.setBounds((new Rectangle(0, 0, 100, 20))
            .withRightOf(this.basicComponentsButton.getBounds())
            .withBottomOf(this.testListBox.getBounds())
            .withTranslationY(5));
        
        final Rectangle dropdown_bounds = this.testSlider.getBounds();
        this.testDropdown.setBounds(dropdown_bounds
            .withBottomOf(dropdown_bounds)
            .translateY(5));
    }
    
    //==================================================================================================================
    @Override
    public void draw(final Canvas canvas)
    {
        // let's draw white text
        canvas.setColour(Colour.WHITE);
        canvas.drawText(
            ("Selected Items: " + this.testListBox.streamSelectedItems().count()),
            (this.testSlider.getRight() + 10),
            (this.testListBox.getBottom() + 10));
        
        final Rectangle gradient_test_bounds = this.getLocalBounds()
            .setTop(this.testDropdown.getBottom())
            .padTop(10);
        
        // let's make some gradient stuff
        canvas.runWithState(() ->
        {
            canvas.addTransform(AffineTransform.translation(gradient_test_bounds.x(), gradient_test_bounds.y()));
            
            final GuiFont font = canvas.getFont();
            font.setSize(FontSize.pixels(20));

            canvas.setGradient(new Gradient(0x77898989, 0xAA323232, Direction.HORIZONTAL));
            canvas.fill();
            
            final TextLayout layout = new TextLayout(
                Component.literal("Hello this is a ")
                    .setStyle(GuiStyle.EMPTY.withStrikethrough(true))
                    .append(Component
                        .literal("horizontal")
                        .setStyle(Style.EMPTY
                            .withStrikethrough(false)
                            .withUnderlined(true)
                            .withColor(Colour.GOLD.colour())))
                    .append(Component.literal(" test gradient")),
                font,
                TextFormat.LAYOUT_OPTIONS);

            canvas.setGradient(new Gradient(0xFFFF0000, 0xFF0000FF, Direction.HORIZONTAL));
            final Rectangle layout_rect = layout.draw(canvas, new Rectangle(10, 10, 0, 0));
            
            canvas.setColour(Colour.WHITE);
            canvas.drawRect(layout_rect);
            
            final TextLayout layout2 = new TextLayout(
                Component.literal("Hello this is a ")
                    .setStyle(GuiStyle.EMPTY
                        .withUnderlined(true)
                        .withItalic(true))
                    .append(Component
                        .literal("vertical")
                        .setStyle(Style.EMPTY
                            .withBold(true)
                            .withItalic(false)
                            .withColor(Colour.GOLD.colour())))
                    .append(Component.literal(" test gradient")),
                font,
                TextFormat.LAYOUT_OPTIONS);
            
            canvas.setGradient(new Gradient(0xFFFFFF00, 0xFF00FF00, Direction.VERTICAL));
            final Rectangle layout2_rect = layout2.draw(canvas, new Rectangle(10, 40, 0, 0));
            
            canvas.setColour(Colour.WHITE);
            canvas.drawRect(layout2_rect);
            
            font.setSize(1f);
            final TextLayout multiline_layout = new TextLayout(
                Component.empty()
                    .append(Component
                        .literal("This text is normal but with a tracking factor of \n0.5, ")
                        .setStyle(GuiStyle.EMPTY.withTracking(0.5f)))
                    .append(Component
                        .literal("hereisalongwordtoolonginfacttofitinalinewithamaximumwidthof300minecraftpixels")
                        .setStyle(GuiStyle.EMPTY
                            .withUnderlined(true)
                            .withSize(2f)
                            .withFontResource(Minecraft.UNIFORM_FONT)
                            .withBackgroundColor(Colour.BLACK.colour())
                            .withColor(Colour.GOLD)))
                    .append(Component
                        .literal(" This text is in the standard galactic alphabet font, so you won't be able to read it, but... yeah. ")
                        .setStyle(GuiStyle.EMPTY
                            .withFontResource(Identifier.withDefaultNamespace("alt"))))
                    .append(Component
                        .literal("This text has a font size of 1.5 em, which means one and a half of the default minecraft size, it is also strikethrough and has purple as shadow colour.")
                        .setStyle(GuiStyle.EMPTY
                            .withSize(1.5f)
                            .withStrikethrough(true)
                            .withShadowColor(Colour.PURPLE)))
                    .append(Component
                        .literal(" This text is nothing special, really... it is just... a teensy bit smaller than the default, 0.5 em to be exact, which is half the default.")
                        .setStyle(GuiStyle.EMPTY
                            .withSize(0.5f))),
                font,
                new TextLayout.LayoutOptions() {{
                    wordWrap  = TextLayout.WordWrap.BREAK_SPACE;
                    maxWidth  = 300;
                    alignment = Alignment.TOP_CENTRE;
                }});
            
            canvas.setColour(Colour.WHITE);
            final Rectangle multilin_layout_rect = multiline_layout.draw(
                canvas,
                new Rectangle(this.getCentreX(), 10, 300, Integer.MAX_VALUE));
            
            canvas.setColour(Colour.WHITE);
            canvas.drawRect(multilin_layout_rect);
        });
        
        canvas.setColour(Colour.GOLD);
        canvas.drawText(
            Component.nullToEmpty("FPS: " + Minecraft.getInstance().getFps()),
            this.getLocalBounds().removeBottom(15).translateX(5).getPosition());
    }
}
