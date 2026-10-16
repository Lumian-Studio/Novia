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
package xyz.lumian.novia.api.gui.font;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Function3;
import net.minecraft.client.ComponentCollector;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.network.chat.*;
import net.minecraft.util.*;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;
import xyz.lumian.novia.api.core.util.Colour;
import xyz.lumian.novia.api.gui.canvas.Canvas;
import xyz.lumian.novia.api.gui.canvas.ICanvasState;
import xyz.lumian.novia.api.gui.canvas.brush.Brush;
import xyz.lumian.novia.api.gui.canvas.brush.FillType;
import xyz.lumian.novia.api.gui.canvas.brush.VertexPalette;
import xyz.lumian.novia.api.gui.canvas.brush.gradient.Gradient;
import xyz.lumian.novia.api.gui.canvas.brush.gradient.IGradientProvider;
import xyz.lumian.novia.api.gui.geometry.Alignment;
import xyz.lumian.novia.api.gui.geometry.Rectangle;
import xyz.lumian.novia.api.gui.impl.BakedSheetGlyphAccessor;
import xyz.lumian.novia.api.gui.impl.GlyphMetricsExtension;
import xyz.lumian.novia.api.gui.util.DrawUtil;

import java.nio.CharBuffer;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Stream;



//**********************************************************************************************************************
public class TextLayout
{
    //******************************************************************************************************************
    /// @param baked        The baked glyph
    /// @param codePoint    The UTF code point of the glyph
    /// @param advance      The width of the glyph (including tracking)
    /// @param ascent       The ascent of the glyph from the top to the baseline
    /// @param shadowOffset The shadow offset of the glyph
    /// @param boldOffset   The boldness offset of the glyph
    /// @param italicOffset The italic offset of the shadow of the glyph
    public record TextGlyph(
        BakedGlyph baked,
        int        codePoint,
        float      advance,
        float      ascent,
        float      scale,
        float      shadowOffset,
        float      boldOffset,
        float      italicOffset
    ) {}
    
    /// @param textGlyphs       The list of glyphs for this run
    /// @param font             The font used to render the text
    /// @param colour           The colour used to render the text
    /// @param backgroundColour The colour used behind the text to draw a highlight
    /// @param shadowColour     The shadow colour used to render the text
    /// @param baseline         The baseline of the font that this run is using
    public record Run(
                  List<TextGlyph> textGlyphs,
                  GuiFont         font,
        @Nullable TextColor       colour,
        @Nullable TextColor       backgroundColour,
        @Nullable Integer         shadowColour,
                  float           baseline
    ) {}
    
    /// @param runs    The collection of text runs this line contains
    /// @param ascent  The ascent of the biggest character, upwards from the baseline
    /// @param descent The descent of the biggest character, downwards from the baseline
    /// @param leading The extra space factor between this and the next line (where 1.0 means normal spacing,
    ///                2.0 means double and so on)
    public record Line(
        List<Run> runs,
        float     width,
        float     ascent,
        float     descent,
        float     leading
    ) {}
    
    /// Describes how the text should be wrapped.
    public enum WordWrap
    {
        /// Do not wrap, discard any excess.
        NO_WRAP,
        
        /// Uses a greedy line breaking algorithm which wraps whenever possible.
        BREAK_WORD,
        
        /// Uses a greedy line breaking algorithm which wraps by word boundary; breaks words only when the word itself
        /// is too long for a line.
        BREAK_SPACE,
    }
    
    /// A set of formatting options of how the text should behave in this layout.
    public static class LayoutOptions
    {
        //**************************************************************************************************************
        /// The maximum width of the layout, exceeding characters will be broken on to the next line.
        public float maxWidth = Float.MAX_VALUE;
        
        /// The maximum height of the layout; lines that are too high will be discarded.
        public float maxHeight = Float.MAX_VALUE;
        
        /// Additional spacing between lines, if negative, lines will be closer together. This value is a factor where
        /// 1.0 means normal spacing and anything else is a multiple of the default.
        public float lineSpacing = 1.15f;
        
        /// If [WordWrap#NO_WRAP] is being used and this is `true`, new lines will be consumed, otherwise new lines will
        /// be replaced with spaces.
        public boolean consumeNewLine = false;
        
        /// The wrap policy of lines.
        public WordWrap wordWrap = WordWrap.BREAK_WORD;
        
        /// Specifies the alignment of the text to be drawn. This is similar to the justification option in text editing
        /// programs like WordPad.
        public Alignment alignment = Alignment.TOP_LEFT;
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private interface WrapHelper
        extends TextFormat.Visitor
    {
        //**************************************************************************************************************
        List<Line> getLines();
        
        float getWidth();
        float getHeight();
    }
    
    private class MultiLineWrapper
        implements WrapHelper
    {
        //**************************************************************************************************************
        private final Deque<Run>  runs = new ArrayDeque<>(4);
        private final Deque<Line> lines;
        
        private float   left           = 0f;
        private float   top            = 0f;
        private float   rectWidth      = 0;
        
        @Nullable
        private Integer lastBreakPoint = null;
        
        //**************************************************************************************************************
        public MultiLineWrapper(final int prealloc) { this.lines = new ArrayDeque<>(prealloc); }
        
        //==============================================================================================================
        @Override
        public List<Line> getLines()
        {
            if (!this.runs.isEmpty())
            {
                this.freezeLine();
            }
            
            return List.copyOf(this.lines);
        }
        
        @Override public float getWidth()  { return this.rectWidth; }
        @Override public float getHeight() { return this.top; }
        
        //==============================================================================================================
        @Override
        public boolean accept(final int i, final TextFormat.Node node, final CharBuffer content)
        {
            final LayoutOptions options = TextLayout.this.layoutOptions;
            final GuiFont       font    = node.font();
            final float         height  = font.getHeight();
            
            if ((this.top + height) > options.maxHeight)
            {
                this.runs.clear();
                return false;
            }
            
            final float   size     = font.getSizeEm();
            final float   tracking = font.getTrackingSpace();
            final float   ascent   = font.getAscent();
            final boolean bold     = font.isBold();
            final boolean shaded   = font.isShaded();
            final boolean italic   = font.isItalic();
            
            final List<TextGlyph> glyphs = new ArrayList<>();
            
            for (final var c : content.codePoints().toArray())
            {
                if (c == '\n')
                {
                    this.addRun(new ArrayList<>(glyphs), node, ascent);
                    this.freezeLine();
                    
                    if ((this.top + height) > options.maxHeight)
                    {
                        return false;
                    }
                    
                    glyphs.clear();
                    continue;
                }
                
                if (c == ' ')
                {
                    this.lastBreakPoint = glyphs.size();
                }
                
                final BakedGlyph glyph       = font.getGlyph(c);
                final GlyphInfo  info        = glyph.info();
                final float      bold_offset = (bold ? (info.getBoldOffset() * size) : 0f);
                final float      scale       = ((GlyphMetricsExtension) info).novia$getScaleFactor();
                final float      advance     = (
                    ((info.getAdvance() * scale * size) + bold_offset)
                    + tracking
                );
                
                if ((this.left + advance) > options.maxWidth)
                {
                    // At a minimum, we must add at least one character, even if it overflows
                    if (glyphs.isEmpty())
                    {
                        if (this.runs.isEmpty())
                        {
                            final TextGlyph text_glyph = this.createGlyph(c, glyph, font, advance, bold_offset, italic,
                                                                          shaded, scale);
                            this.addRun(Collections.singletonList(text_glyph), node, ascent);
                        }
                        
                        this.freezeLine();
                        
                        if ((this.top + height) > options.maxHeight)
                        {
                            return false;
                        }
                    }
                    
                    // we ignore spaces at the end
                    else if (c == ' ')
                    {
                        continue;
                    }
                    else if (options.wordWrap == WordWrap.BREAK_SPACE && this.lastBreakPoint != null)
                    {
                        if (this.lastBreakPoint < 0)
                        {
                            this.repriseRuns();
                            
                            if (this.runs.stream()
                                    .anyMatch(run -> (this.top + run.font.getHeight()) > options.maxHeight))
                            {
                                return false;
                            }
                            
                            this.left = (float) Stream
                                .concat(this.runs.stream().flatMap(r -> r.textGlyphs.stream()), glyphs.stream())
                                .mapToDouble(TextGlyph::advance)
                                .sum();
                        }
                        else
                        {
                            final List<TextGlyph> pre_line_glyphs = glyphs.subList(0, this.lastBreakPoint);
                            this.addRun(new ArrayList<>(pre_line_glyphs), node, ascent);
                            pre_line_glyphs.clear();
                            
                            if (!glyphs.isEmpty())
                            {
                                glyphs.removeFirst();
                            }
                            
                            this.freezeLine();
                            this.left = (float) glyphs.stream().mapToDouble(TextGlyph::advance).sum();
                            
                            if ((this.top + height) > options.maxHeight)
                            {
                                return false;
                            }
                        }
                    }
                    else
                    {
                        this.addRun(new ArrayList<>(glyphs), node, ascent);
                        glyphs.clear();
                        
                        this.freezeLine();
                        
                        if ((this.top + height) > options.maxHeight)
                        {
                            return false;
                        }
                    }
                }
                
                glyphs.addLast(this.createGlyph(c, glyph, font, advance, bold_offset, italic, shaded, scale));
                this.left += advance;
            }
            
            if (!glyphs.isEmpty())
            {
                if (this.lastBreakPoint != null)
                {
                    this.lastBreakPoint -= glyphs.size();
                }
                
                this.addRun(glyphs, node, ascent);
            }
            
            return true;
        }
        
        //--------------------------------------------------------------------------------------------------------------
        private void repriseRuns()
        {
            final var        it            = this.runs.reversed().iterator();
            final Deque<Run> followed_runs = new ArrayDeque<>(2);
            
            while (it.hasNext())
            {
                final Run run = it.next();
                this.lastBreakPoint += run.textGlyphs.size();
                it.remove();
                
                if (this.lastBreakPoint >= 0)
                {
                    final List<TextGlyph> glyphs = new ArrayList<>(run.textGlyphs);
                    
                    if (this.lastBreakPoint > 0)
                    {
                        this.runs.addLast(new Run(
                            ImmutableList.copyOf(glyphs.subList(0, this.lastBreakPoint)),
                            run.font,
                            run.colour,
                            run.backgroundColour,
                            run.shadowColour,
                            run.baseline));
                    }
                    
                    if (this.lastBreakPoint < (run.textGlyphs.size() - 1))
                    {
                        followed_runs.addFirst(new Run(
                            ImmutableList.copyOf(glyphs.subList((this.lastBreakPoint + 1), glyphs.size())),
                            run.font,
                            run.colour,
                            run.backgroundColour,
                            run.shadowColour,
                            run.baseline));
                    }
                    
                    this.freezeLine();
                    break;
                }
                
                followed_runs.addFirst(run);
            }
            
            this.runs.addAll(followed_runs);
        }
        
        private TextGlyph createGlyph(final int codePoint, final BakedGlyph glyph, final GuiFont font,
                                      final float advance, final float boldOffset, final boolean italic,
                                      final boolean shaded, final float scale)
        {
            final GlyphInfo info          = glyph.info();
            final float     size          = font.getSizeEm();
            final float     shadow_offset = (shaded ? (info.getShadowOffset() * size) : 0f);
            final float     italic_offset = (italic ? (0.25f                  * size) : 0f);
            return new TextGlyph(glyph, codePoint, advance,
                                 (((GlyphMetricsExtension) info).novia$getAscent() * scale * size),
                                 scale, shadow_offset, boldOffset, italic_offset);
        }
        
        private void addRun(final List<TextGlyph> glyphs, final TextFormat.Node node, final float baseline)
        {
            this.runs.addLast(new Run(ImmutableList.copyOf(glyphs), node.font(), node.colour(), node.backgroundColour(),
                                      node.shadowColour(), baseline));
        }
        
        private void freezeLine()
        {
            float ascent  = -Float.MAX_VALUE;
            float descent = -Float.MAX_VALUE;
            float width   = 0f;
            
            for (final var run : this.runs)
            {
                ascent  = Math.max(ascent,  run.font.getAscent());
                descent = Math.max(descent, run.font.getDescent());
                width  += (float) run.textGlyphs.stream().mapToDouble(TextGlyph::advance).sum();
            }
            
            final float line_spacing = TextLayout.this.layoutOptions.lineSpacing;
            
            this.lines.addLast(new Line(List.copyOf(this.runs), width, ascent, descent, line_spacing));
            this.runs.clear();
            
            this.rectWidth      = Math.max(this.rectWidth, (width + 2));
            this.top           += ((ascent + descent) * line_spacing);
            this.left           = 0f;
            this.lastBreakPoint = null;
        }
    }
    
    private class SingleLineWrapper
        implements WrapHelper
    {
        //**************************************************************************************************************
        private final List<Run> runs;
        
        private float left           = 0;
        private float maxLineHeight  = Integer.MIN_VALUE;
        private float maxAscent      = Integer.MIN_VALUE;
        private float maxDescent     = Integer.MIN_VALUE;
        
        //**************************************************************************************************************
        public SingleLineWrapper(final int nodeCount) { this.runs = new ArrayList<>(nodeCount); }
        
        //==============================================================================================================
        @Override
        public List<Line> getLines()
        {
            if (this.maxLineHeight > TextLayout.this.layoutOptions.maxHeight || this.runs.isEmpty())
            {
                return List.of();
            }
            
            return Collections.singletonList(new Line(this.runs, this.left, this.maxAscent, this.maxDescent,
                                                      TextLayout.this.layoutOptions.lineSpacing));
        }
        
        @Override public float getWidth()  { return (this.left + 2); }
        @Override public float getHeight() { return this.maxLineHeight; }
        
        //==============================================================================================================
        @Override
        public boolean accept(final int i, final TextFormat.Node node, final CharBuffer content)
        {
            final GuiFont font   = node.font();
            final float   height = font.getHeight();
            final float   size   = font.getSizeEm();
            final boolean bold   = node.font().isBold();
            final boolean shaded = node.font().isShaded();
            final boolean italic = node.font().isItalic();
            
            this.maxLineHeight = Math.max(this.maxLineHeight, height);
            
            if (height > TextLayout.this.layoutOptions.maxHeight)
            {
                return false;
            }
            
            final List<TextGlyph> chars = content
                .codePoints()
                .mapToObj(code_point ->
                {
                    final BakedGlyph glyph         = font.getGlyph(code_point);
                    final GlyphInfo  info          = glyph.info();
                    final float      bold_offset   = (bold   ? (info.getBoldOffset()   * size) : 0f);
                    final float      shadow_offset = (shaded ? (info.getShadowOffset() * size) : 0f);
                    final float      italic_offset = (italic ? (0.25f                   * size) : 0f);
                    final float      scale         = ((GlyphMetricsExtension) info).novia$getScaleFactor();
                    
                    return new TextGlyph(glyph, code_point, ((info.getAdvance() * scale * size) + bold_offset),
                                         (((GlyphMetricsExtension) info).novia$getAscent() * scale * size),
                                         scale, shadow_offset, bold_offset, italic_offset);
                })
                .toList();
            
            final TextGlyph space_glyph;
            {
                final int        space         = ' ';
                final BakedGlyph glyph         = font.getGlyph(space);
                final GlyphInfo  info          = glyph.info();
                final float      bold_offset   = (bold   ? (info.getBoldOffset()   * size) : 0f);
                final float      shadow_offset = (shaded ? (info.getShadowOffset() * size) : 0f);
                final float      italic_offset = (italic ? (0.25f                   * size) : 0f);
                final float      scale         = ((GlyphMetricsExtension) info).novia$getScaleFactor();
                
                space_glyph = new TextGlyph(glyph, space, ((info.getAdvance() * scale * size) + bold_offset),
                                            (((GlyphMetricsExtension) info).novia$getAscent() * scale * size),
                                            scale, shadow_offset, bold_offset, italic_offset);
            }
            
            final float tracking = font.getTrackingSpace();
            final var   it       = chars.listIterator();
            
            boolean quit = false;
            
            while (it.hasNext())
            {
                TextGlyph t_glyph = it.next();
                
                if (t_glyph.codePoint == '\n')
                {
                    if (TextLayout.this.layoutOptions.consumeNewLine)
                    {
                        it.remove();
                        continue;
                    }
                    
                    it.set(space_glyph);
                    t_glyph = space_glyph;
                }
                
                this.left += (tracking + t_glyph.advance);
                
                if (this.left > TextLayout.this.layoutOptions.maxWidth)
                {
                    quit = true;
                    break;
                }
            }
            
            final float ascent = font.getAscent();
            this.maxAscent  = Math.max(this.maxAscent,  ascent);
            this.maxDescent = Math.max(this.maxDescent, font.getDescent());
            
            this.runs.add(new Run(chars, font, node.colour(), node.backgroundColour(), node.shadowColour(), ascent));
            
            return !quit;
        }
    }
    
    //==================================================================================================================
    static abstract class StatefulVisitor
        implements ICharacterVisitor
    {
        //**************************************************************************************************************
        @Nullable
        private GuiFont font = null;
        
        //**************************************************************************************************************
        public void prepare(GuiFont font) {}
        public abstract boolean accept(int i, GuiFont font, int codePoint);
        
        //==============================================================================================================
        @Override
        public boolean accept(final int i, final GuiFont font, final Style style, final int codePoint)
        {
            if (this.font != font)
            {
                this.font = font;
                this.prepare(font);
            }
            
            return this.accept(i, font, codePoint);
        }
    }
    
    static class WidthCollectingVisitor
        extends StatefulVisitor
    {
        //**************************************************************************************************************
        private float width    = 0f;
        private float tracking = 0f;
        
        //**************************************************************************************************************
        public final float getWidth() { return this.width; }
        
        //==============================================================================================================
        @Override public void prepare(final GuiFont font) { this.tracking = font.getTrackingSpace(); }
        
        @Override
        public boolean accept(final int i, final GuiFont font, final int codePoint)
        {
            this.width += (font.getCharWidth(codePoint) + this.tracking);
            return true;
        }
    }
    
    static class WidthLimitingVisitor
        extends StatefulVisitor
    {
        //**************************************************************************************************************
        private float tracking = 0f;
        private int   length   = 0;
        private float width;
        
        //**************************************************************************************************************
        public WidthLimitingVisitor(final int maxWidth) { this.width = maxWidth; }
        
        //==============================================================================================================
        public int getLength() { return this.length; }
        
        //==============================================================================================================
        @Override public void prepare(final GuiFont font) { this.tracking = font.getTrackingSpace(); }
        
        @Override
        public boolean accept(final int i, final GuiFont font, final int codePoint)
        {
            this.width -= (font.getCharWidth(codePoint) + this.tracking);
            
            if (this.width >= 0f)
            {
                this.length += Character.charCount(codePoint);
                return true;
            }
            
            return false;
        }
        
        public void resetLength() { this.length = 0; }
    }
    
    static class TrimmedOrderedText
        extends WidthLimitingVisitor
        implements FormattedCharSequence
    {
        //**************************************************************************************************************
        private final GuiFont               baseFont;
        private final FormattedCharSequence input;
        
        @Nullable
        private FormattedCharSink visitor;
        
        //**************************************************************************************************************
        public TrimmedOrderedText(final GuiFont baseFont, final FormattedCharSequence input, final int maxWidth)
        {
            super(maxWidth);
            this.input    = input;
            this.baseFont = baseFont;
        }
        
        //==============================================================================================================
        @Override
        public boolean accept(final int i, final GuiFont font, final Style style, final int codePoint)
        {
            if (super.accept(i, font, style, codePoint))
            {
                Objects.requireNonNull(this.visitor).accept(i, style, codePoint);
                return true;
            }
            
            return false;
        }
        
        @Override
        public boolean accept(final FormattedCharSink visitor)
        {
            this.visitor = visitor;
            
            final boolean result = this.visitFormatted(this.baseFont, this.input);
            this.resetLength();
            
            return result;
        }
    }
    
    @FunctionalInterface
    interface GenericVisitor<T> extends Function3<ICharacterVisitor, GuiFont, T, Boolean> {}
    
    //==================================================================================================================
    private record DrawableGlyph(
                  BakedSheetGlyphAccessor baked,
                  VertexPalette           textPalette,
        @Nullable VertexPalette           shadowPalette,
                  float                   x,
                  float                   y,
                  float                   scale,
                  float                   size,
                  float                   shadowOffset,
                  float                   boldOffset,
                  float                   italicOffset
    )
    {
        //**************************************************************************************************************
        public float getItalicOffsetAtMaxY() { return (0.25f * this.baked.novia$down() * this.scale); }
        public float getItalicOffsetAtMinY() { return (0.25f * this.baked.novia$up()   * this.scale); }
        
        //==============================================================================================================
        public boolean isShaded() { return (this.shadowPalette != null); }
        public boolean isItalic() { return (this.italicOffset   > 0f);   }
        public boolean isBold()   { return (this.boldOffset     > 0f);   }
        
        //==============================================================================================================
        public void draw(final VertexConsumer consumer, final Matrix4f matrix, final int light)
        {
            if (this.isShaded())
            {
                final float shaded_x = (this.x + this.shadowOffset - this.italicOffset);
                final float shaded_y = (this.y + this.shadowOffset);
                
                this.drawGlyphVerts(shaded_x, shaded_y, matrix, consumer, this.shadowPalette, light);
                
                if (this.boldOffset > 0f)
                {
                    this.drawGlyphVerts((shaded_x + this.boldOffset), shaded_y, matrix, consumer, this.shadowPalette,
                                        light);
                }
            }
    
            this.drawGlyphVerts(this.x, this.y, matrix, consumer, this.textPalette, light);
    
            if (this.boldOffset > 0f)
            {
                this.drawGlyphVerts((this.x + this.boldOffset), this.y, matrix, consumer, this.textPalette,
                                    light);
            }
        }
        
        //--------------------------------------------------------------------------------------------------------------
        private void drawGlyphVerts(final float x, final float y,
                                    final Matrix4f matrix, final VertexConsumer vertexConsumer,
                                    final VertexPalette palette, final int light)
        {
            final boolean italic      = this.isItalic();
            final float   x1          = (x + this.baked.novia$left()  * this.scale);
            final float   y1          = (y + this.baked.novia$up()    * this.scale);
            final float   x2          = (x + this.baked.novia$right() * this.scale);
            final float   y2          = (y + this.baked.novia$down()  * this.scale);
            final float   italic_y1   = (italic ? this.getItalicOffsetAtMaxY() : 0.0f);
            final float   italic_y2   = (italic ? this.getItalicOffsetAtMinY() : 0.0f);
            final float   bold_offset = (this.isBold() ? (0.1f * this.size) : 0f);
    
            palette.accept((tl, tr, bl, br) ->
            {
                vertexConsumer
                    .addVertex(matrix, (x1 + italic_y1 - bold_offset), (y1 - bold_offset), 0f)
                    .setUv(this.baked.novia$u0(), this.baked.novia$v0())
                    .setLight(light)
                    .setColor(tl);
                vertexConsumer
                    .addVertex(matrix, (x1 + italic_y2 - bold_offset), (y2 + bold_offset), 0f)
                    .setUv(this.baked.novia$u0(), this.baked.novia$v1())
                    .setLight(light)
                    .setColor(bl);
                vertexConsumer
                    .addVertex(matrix, (x2 + italic_y2 + bold_offset), (y2 + bold_offset), 0f)
                    .setUv(this.baked.novia$u1(), this.baked.novia$v1())
                    .setLight(light)
                    .setColor(br);
                vertexConsumer
                    .addVertex(matrix, (x2 + italic_y1 + bold_offset), (y1 - bold_offset), 0f)
                    .setUv(this.baked.novia$u1(), this.baked.novia$v0())
                    .setLight(light)
                    .setColor(tr);
            });
        }
    }
    
    private record GlyphRenderer(Collection<DrawableGlyph> glyphs)
        implements ICanvasState.Renderer
    {
        //**************************************************************************************************************
        @Override
        public void render(final VertexConsumer vertices, final Matrix3x2f matrix)
        {
            final Matrix4f matrix4f = (new Matrix4f()).mul(matrix);
            
            for (final var glyph : this.glyphs)
            {
                glyph.draw(vertices, matrix4f, 15728880);
            }
        }
    }
    
    private record LineRenderer(
                  float         x,
                  float         y,
                  float         width,
                  float         height,
                  float         shadowOffset,
                  VertexPalette colourPalette,
        @Nullable VertexPalette shadowPalette,
                  int           light
    )
        implements ICanvasState.Renderer
    {
        //**************************************************************************************************************
        @Override
        public void render(final VertexConsumer vertices, final Matrix3x2f matrix)
        {
            if (this.shadowPalette != null)
            {
                this.drawRectangle(vertices, matrix, (this.x + shadowOffset), (this.y + this.shadowOffset),
                                   this.shadowPalette);
            }
    
            this.drawRectangle(vertices, matrix, this.x, this.y, this.colourPalette);
        }
        
        //--------------------------------------------------------------------------------------------------------------
        private void drawRectangle(final VertexConsumer vertices, final Matrix3x2f matrix,
                                   final float x, final float y, final VertexPalette palette)
        {
            palette.accept((tl, tr, bl, br) ->
                DrawUtil.drawRect((new Matrix4f()).mul(matrix), vertices, 0f, x, y, (x + this.width), (y + this.height),
                                  tl, tr, bl, br, this.light));
        }
    }
    
    //******************************************************************************************************************
    /// Gets the exact width needed to fully represent the given text. Other than [GuiFont#getTextWidth(String)],
    /// this will consume formatting codes.
    /// @param baseFont The base [GuiFont] to use if not overridden by style attributes
    /// @param text     The text to measure
    /// @return The exact width of the text
    public static float getTextWidth(final GuiFont baseFont, final String text)
    {
        return TextLayout.getTextWidth_impl(ICharacterVisitor::visitFormatted, baseFont, text);
    }
    
    /// Gets the exact width needed to fully represent the given text. Formatting codes will be consumed.
    /// @param baseFont The base [GuiFont] to use if not overridden by style attributes
    /// @param text     The text to measure
    /// @return The exact width of the text
    public static float getTextWidth(final GuiFont baseFont, final FormattedText text)
    {
        return TextLayout.getTextWidth_impl(ICharacterVisitor::visitFormatted, baseFont, text);
    }
    
    /// Gets the exact width needed to fully represent the given text.
    /// @param baseFont The base [GuiFont] to use if not overridden by style attributes
    /// @param text     The text to measure
    /// @return The exact width of the text
    public static float getTextWidth(final GuiFont baseFont, final Component text)
    {
        return TextLayout.getTextWidth_impl(ICharacterVisitor::visitFormatted, baseFont, text);
    }
    
    /// Gets the exact width needed to fully represent the given text.
    /// @param baseFont The base [GuiFont] to use if not overridden by style attributes
    /// @param text     The text to measure
    /// @return The exact width of the text
    public static float getTextWidth(final GuiFont baseFont, final FormattedCharSequence text)
    {
        return TextLayout.getTextWidth_impl(ICharacterVisitor::visitFormatted, baseFont, text);
    }
    
    /// Gets the rounded up width needed to fully contain the given text. Other than
    /// [GuiFont#getTextWidthFitted(String)], this will consume formatting codes.
    /// @param baseFont The base [GuiFont] to use if not overridden by style attributes
    /// @param text     The text to measure
    /// @return The rounded up width of the text
    public static int getTextWidthFitted(final GuiFont baseFont, final String text)
    {
        return Mth.ceil(TextLayout.getTextWidth(baseFont, text));
    }
    
    /// Gets the rounded up width needed to fully contain the given text. Formatting codes will be consumed.
    /// @param baseFont The base [GuiFont] to use if not overridden by style attributes
    /// @param text     The text to measure
    /// @return The rounded up width of the text
    public static int getTextWidthFitted(final GuiFont baseFont, final FormattedText text)
    {
        return Mth.ceil(TextLayout.getTextWidth(baseFont, text));
    }
    
    /// Gets the rounded up width needed to fully contain the given text.
    /// @param baseFont The base [GuiFont] to use if not overridden by style attributes
    /// @param text     The text to measure
    /// @return The rounded up width of the text
    public static int getTextWidthFitted(final GuiFont baseFont, final Component text)
    {
        return Mth.ceil(TextLayout.getTextWidth(baseFont, text));
    }
    
    /// Gets the rounded up width needed to fully contain the given text.
    /// @param baseFont The base [GuiFont] to use if not overridden by style attributes
    /// @param text     The text to measure
    /// @return The rounded up width of the text
    public static int getTextWidthFitted(final GuiFont baseFont, final FormattedCharSequence text)
    {
        return Mth.ceil(TextLayout.getTextWidth(baseFont, text));
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private static <T> float getTextWidth_impl(final GenericVisitor<T> func, final GuiFont baseFont, final T text)
    {
        final WidthCollectingVisitor visitor = new WidthCollectingVisitor();
        func.apply(visitor, baseFont, text);
        return visitor.getWidth();
    }
    
    //==================================================================================================================
    /// Gets the number of characters that fit into the given maximum width. Other than
    /// [GuiFont#getTextLength(String, int)], this will consume formatting codes.
    /// @param baseFont The base [GuiFont] to use if not overridden by style attributes
    /// @param text     The text to measure
    /// @param maxWidth The maximum width the text can fit into
    /// @return The number of characters that fit into the given constraint (multibyte codepoints count as one)
    public static int getTextLength(final GuiFont baseFont, final String text, final int maxWidth)
    {
        return TextLayout.getTextLength_impl(ICharacterVisitor::visitFormatted, baseFont, text, maxWidth);
    }
    
    /// Gets the number of characters that fit into the given maximum width. Formatting codes will be consumed.
    /// @param baseFont The base [GuiFont] to use if not overridden by style attributes
    /// @param text     The text to measure
    /// @param maxWidth The maximum width the text can fit into
    /// @return The number of characters that fit into the given constraint (multibyte codepoints count as one)
    public static int getTextLength(final GuiFont baseFont, final FormattedText text, final int maxWidth)
    {
        return TextLayout.getTextLength_impl(ICharacterVisitor::visitFormatted, baseFont, text, maxWidth);
    }
    
    /// Gets the number of characters that fit into the given maximum width.
    /// @param baseFont The base [GuiFont] to use if not overridden by style attributes
    /// @param text     The text to measure
    /// @param maxWidth The maximum width the text can fit into
    /// @return The number of characters that fit into the given constraint (multibyte codepoints count as one)
    public static int getTextLength(final GuiFont baseFont, final Component text, final int maxWidth)
    {
        return TextLayout.getTextLength_impl(ICharacterVisitor::visitFormatted, baseFont, text, maxWidth);
    }
    
    /// Gets the number of characters that fit into the given maximum width.
    /// @param baseFont The base [GuiFont] to use if not overridden by style attributes
    /// @param text     The text to measure
    /// @param maxWidth The maximum width the text can fit into
    /// @return The number of characters that fit into the given constraint (multibyte codepoints count as one)
    public static int getTextLength(final GuiFont baseFont, final FormattedCharSequence text, final int maxWidth)
    {
        return TextLayout.getTextLength_impl(ICharacterVisitor::visitFormatted, baseFont, text, maxWidth);
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private static <T> int getTextLength_impl(final GenericVisitor<T> func, final GuiFont baseFont, final T text,
                                              final int maxWidth)
    {
        final WidthLimitingVisitor visitor = new WidthLimitingVisitor(maxWidth);
        func.apply(visitor, baseFont, text);
        return visitor.getLength();
    }
    
    //==================================================================================================================
    /// Trims the given text so that it doesn't exceed the given maximum width and returns the trimmed text. Other than
    /// [GuiFont#trimToWidth(String, int)], this will consume formatting codes.
    /// @param baseFont The base [GuiFont] to use if not overridden by style attributes
    /// @param text     The text to trim
    /// @param maxWidth The maximum width the text can fit into
    /// @return The trimmed text
    public String trimToWidth(final GuiFont baseFont, final String text, final int maxWidth)
    {
        return text.substring(0, TextLayout.getTextLength(baseFont, text, maxWidth));
    }
    
    /// Trims the given text so that it doesn't exceed the given maximum width and returns the trimmed text. Formatting
    /// codes will be consumed.
    /// @param baseFont The base [GuiFont] to use if not overridden by style attributes
    /// @param text     The text to trim
    /// @param maxWidth The maximum width the text can fit into
    /// @return The trimmed text
    public static FormattedText trimToWidth(final GuiFont baseFont, final FormattedText text, final int maxWidth)
    {
        final WidthLimitingVisitor visitor = new WidthLimitingVisitor(maxWidth);
        return text
            .visit(
                new FormattedText.StyledContentConsumer<FormattedText>()
                {
                    //**************************************************************************************************
                    private final ComponentCollector collector = new ComponentCollector();

                    //**************************************************************************************************
                    public Optional<FormattedText> accept(final Style style, final String string)
                    {
                        visitor.resetLength();

                        if (!visitor.visitFormatted(baseFont, string))
                        {
                            final String string2 = string.substring(0, visitor.getLength());

                            if (!string2.isEmpty())
                            {
                                this.collector.append(FormattedText.of(string2, style));
                            }

                            return Optional.of(this.collector.getResultOrEmpty());
                        }

                        if (!string.isEmpty())
                        {
                            this.collector.append(FormattedText.of(string, style));
                        }

                        return Optional.empty();
                    }
                },
                Style.EMPTY)
            .orElse(text);
    }
    
    /// Trims the given text so that it doesn't exceed the given maximum width and returns the trimmed text. Formatting
    /// codes will be consumed.
    /// @param baseFont The base [GuiFont] to use if not overridden by style attributes
    /// @param text     The text to trim
    /// @param maxWidth The maximum width the text can fit into
    /// @return The trimmed text
    public static Component trimToWidth(final GuiFont baseFont, final Component text, final int maxWidth)
    {
        final WidthLimitingVisitor visitor = new WidthLimitingVisitor(maxWidth);
        return text
            .visit(
                new FormattedText.StyledContentConsumer<Component>()
                {
                    //**************************************************************************************************
                    private final MutableComponent collector = Component.empty();

                    //**************************************************************************************************
                    public Optional<Component> accept(final Style style, final String string)
                    {
                        visitor.resetLength();

                        if (!visitor.visitFormatted(baseFont, string))
                        {
                            final String string2 = string.substring(0, visitor.getLength());

                            if (!string2.isEmpty())
                            {
                                this.collector.append(Component.literal(string2).setStyle(style));
                            }

                            return Optional.of(this.collector);
                        }

                        if (!string.isEmpty())
                        {
                            this.collector.append(Component.literal(string).setStyle(style));
                        }

                        return Optional.empty();
                    }
                },
                Style.EMPTY)
            .orElse(text);
    }
    
    /// Trims the given text so that it doesn't exceed the given maximum width and returns the trimmed text. Formatting
    /// codes will be consumed.
    /// @param baseFont The base [GuiFont] to use if not overridden by style attributes
    /// @param text     The text to trim
    /// @param maxWidth The maximum width the text can fit into
    /// @return The trimmed text
    public static FormattedCharSequence trimToWidth(final GuiFont               baseFont,
                                                    final FormattedCharSequence text,
                                                    final int                   maxWidth)
    {
        return new TrimmedOrderedText(baseFont, text, maxWidth);
    }
    
    //******************************************************************************************************************
    private final List<Line>    lines = new ArrayList<>();
    private final LayoutOptions layoutOptions;
    
    private int width  = 0;
    private int height = 0;
    
    //******************************************************************************************************************
    /// Creates a new [TextLayout] from the given format.
    /// @param textFormat The [TextFormat] to parse
    /// @param options    The layout options to use to parse the input
    public TextLayout(final TextFormat textFormat, final LayoutOptions options)
    {
        this.layoutOptions = options;
        this.computeLayout(textFormat);
    }
    
    /// Creates a new [TextLayout] from the given text.
    /// @param text     The text to parse
    /// @param baseFont The base {@link GuiFont} to use if not overridden by style attributes
    /// @param options  The layout options to use to parse the input
    public TextLayout(final String text, final GuiFont baseFont, final LayoutOptions options)
    {
        this(Util.make(new TextFormat(), (format -> format.append(text, baseFont))), options);
    }
    
    /// Creates a new [TextLayout] from the given text.
    /// @param text     The text to parse
    /// @param baseFont The base {@link GuiFont} to use if not overridden by style attributes
    /// @param options  The layout options to use to parse the input
    public TextLayout(final Component text, final GuiFont baseFont, final LayoutOptions options)
    {
        this(Util.make(new TextFormat(), (format -> format.append(text, baseFont))), options);
    }
    
    /// Creates a new [TextLayout] from the given text.
    /// @param text     The text to parse
    /// @param baseFont The base {@link GuiFont} to use if not overridden by style attributes
    /// @param options  The layout options to use to parse the input
    public TextLayout(final FormattedCharSequence text, final GuiFont baseFont, final LayoutOptions options)
    {
        this(Util.make(new TextFormat(), (format -> format.append(text, baseFont))), options);
    }
    
    //==================================================================================================================
    /// Gets a copy of the layout's lines as list.
    /// @return The list of [Line] objects
    public List<Line> getLines() { return new ArrayList<>(this.lines); }
    
    /// Gets the [Line] at the specified index.
    /// @return The [Line]
    /// @throws IndexOutOfBoundsException If the index is outside the bounds
    public Line getLine(final int index) { return this.lines.get(index); }
    
    /// Gets the number of lines this layout contains.
    /// @return The number of lines
    public int getNumLines() { return this.lines.size(); }
    
    /// {@return the width of the layout}
    public int getWidth() { return this.width; }
    
    /// {@return the height of the layout}
    public int getHeight() { return this.height; }
    
    //==================================================================================================================
    /// Adds the given line to the layout. Do note that the line will not be evaluated as the text upon construction is,
    /// that means any constraints or layout options will be ignored for this line.
    /// @param line The [Line] to add
    public void addLine(final Line line) { this.lines.add(line); }
    
    //==================================================================================================================
    /// Draws the layout within the given area, if the text is bigger it will extend out of it and depending on the
    /// given [LayoutOptions#alignment], will align it to the available space.
    /// @param canvas The [Canvas] to draw onto
    /// @param area   The [Rectangle] to align the text to
    public Rectangle draw(final Canvas canvas, final Rectangle area)
    {
        if (this.lines.isEmpty())
        {
            return area.withSize(0, 0);
        }
        
        final Deque<DrawableGlyph> drawables  = new ArrayDeque<>();
        final Brush                brush      = canvas.getBrush();
        final Alignment            alignment  = this.layoutOptions.alignment;
        final Rectangle            client_box = alignment.align(area, this.width, this.height);
        final Rectangle            target_box = client_box.withPadding(1, 0);
        final float                opacity    = canvas.getOpacity();
        final Gradient             gradient   = (brush.getGradient() != null
            ? brush.getGradient().withOpacity(opacity)
            : null);
        
        float             baseline = target_box.y();
        IGradientProvider provider = (switch (brush.getType())
        {
            case SOLID -> IGradientProvider
                .solid(new Colour(Objects.requireNonNull(brush.getColour())).withOpacityRel(opacity));
            case TEXTURED -> IGradientProvider.solid(Colour.WHITE.withOpacityRel(opacity));
            case GRADIENT -> null;
        });

        final GpuSampler sampler = RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST);
        final BiFunction<@Nullable GpuTextureView, @Nullable AbstractTexture, TextureSetup> texturise = ((tex1, tex2) ->
            new TextureSetup(
                tex1,                            (tex2 != null ? tex2.getTextureView() : null), null,
                (tex1 != null ? sampler : null), (tex2 != null ? tex2.getSampler()     : null), null));
        
        for (final var line : this.lines)
        {
            final float line_height = (line.ascent + line.descent);
            final float line_top    = baseline;
            
            float line_left = alignment.align(target_box, Mth.ceil(line.width), this.height).x();
            
            baseline += line.ascent;
            
            if (brush.getType() == FillType.GRADIENT)
            {
                assert (gradient != null);
                provider = IGradientProvider.of(gradient, this.width, line_height);
            }
            
            GpuTextureView texture = null;
            
            final Deque<ICanvasState> states = new ArrayDeque<>(line.runs.size());
            
            RenderPipeline pipeline = null;
            
            for (final var run : line.runs)
            {
                final GuiFont font       = run.font;
                final float   size       = font.getSizeEm();
                final boolean shaded     = font.isShaded();
                final float   run_top    = (baseline - run.baseline);
                final float   run_height = font.getHeight();
                final float   run_left   = line_left;
                
                float run_advance = 0f;
                
                for (final var glyph : run.textGlyphs)
                {
                    final BakedSheetGlyphAccessor baked    = (BakedSheetGlyphAccessor) glyph.baked;
                    final GlyphInfo               info     = glyph.baked.info();
                    final GpuTextureView          texture2 = baked.novia$texture();
                    
                    pipeline = baked.novia$renderTypes().guiPipeline();
                    
                    if (texture2 != texture)
                    {
                        if (!drawables.isEmpty() && run_advance > 0f)
                        {
                            states.addLast(new ICanvasState.Renderable(
                                new GlyphRenderer(new ArrayList<>(drawables)),
                                texturise.apply(texture, brush.getTexture()), pipeline,
                                new Rectangle(Mth.floor(line_left),  Mth.floor(run_top),
                                              Mth.ceil(run_advance), Mth.ceil(run_height))));
                        }
        
                        drawables.clear();
                        
                        line_left  += run_advance;
                        run_advance = 0f;
                        texture     = texture2;
                    }
                    
                    final VertexPalette text_palette;
                    VertexPalette shadow_palette = null;
                    
                    if (run.colour == null)
                    {
                        final float origin_x = (line_left + run_advance - area.x());
                        
                        assert (provider != null);
                        text_palette = provider.getPalette(
                            (origin_x           + (baked.novia$left()  * glyph.scale * size)),
                            (run_top - line_top + (baked.novia$up()    * glyph.scale * size)),
                            (origin_x           + (baked.novia$right() * glyph.scale * size)),
                            (run_top - line_top + (baked.novia$down()  * glyph.scale * size)));
                        
                        if (shaded)
                        {
                            shadow_palette = (run.shadowColour == null
                                ? new VertexPalette(
                                    ARGB.scaleRGB(text_palette.topLeft(),     0.25f),
                                    ARGB.scaleRGB(text_palette.topRight(),    0.25f),
                                    ARGB.scaleRGB(text_palette.bottomLeft(),  0.25f),
                                    ARGB.scaleRGB(text_palette.bottomRight(), 0.25f))
                                : new VertexPalette((new Colour(run.shadowColour)).withOpacityRel(opacity)));
                        }
                    }
                    else
                    {
                        final Colour colour = Colour.fromRgb(run.colour.getValue()).withOpacityRel(opacity);
                        text_palette = new VertexPalette(colour);
                        
                        if (shaded)
                        {
                            shadow_palette = (run.shadowColour == null
                                ? new VertexPalette(ARGB.scaleRGB(colour.colour(), 0.25f))
                                : new VertexPalette((new Colour(run.shadowColour)).withOpacityRel(opacity)));
                        }
                    }
                    
                    drawables.add(new DrawableGlyph(baked, text_palette, shadow_palette, (line_left + run_advance),
                                                    (baseline - glyph.ascent), (glyph.scale * size), size,
                                                    glyph.shadowOffset, glyph.boldOffset, glyph.italicOffset));
                    run_advance += glyph.advance;
                }
                
                if (!drawables.isEmpty() && run_advance > 0f)
                {
                    states.addLast(new ICanvasState.Renderable(
                        new GlyphRenderer(
                            new ArrayList<>(drawables)),
                            texturise.apply(texture, brush.getTexture()),
                            pipeline,
                            new Rectangle(
                                Mth.floor(line_left),
                                Mth.floor(run_top),
                                Mth.ceil(run_advance),
                                Mth.ceil(run_height))));
                }
                
                drawables.clear();
                
                line_left += run_advance;
                
                final float line_start = (run_left - 1f);
                final float line_width = (line_left - run_left + 1f);
                
                if (run.backgroundColour != null && run_advance > 0f)
                {
                    final Rectangle bg_bounds = new Rectangle(
                        Mth.floor(line_start),
                        Mth.floor(line_top),
                        Mth.ceil(line_width),
                        Mth.ceil(line_height));
                    final int colour = Colour.fromRgb(run.backgroundColour.getValue()).withOpacityRel(opacity).colour();
                    canvas.draw(new ICanvasState.Renderable(
                        (((vertices, matrix) -> DrawUtil.drawRect(
                            (new Matrix4f()).mul(matrix),
                            vertices,
                            0f,
                            line_start,
                            line_top,
                            (line_start + line_width),
                            (line_top + line_height),
                            colour, colour, colour, colour))),
                        null,
                        null,
                        bg_bounds));
                }
                
                states.forEach(canvas::draw);
                states.clear();
                
                if (run_advance > 0f)
                {
                    Objects.requireNonNull(provider);
                    
                    if (font.isUnderlined())
                    {
                        final float line_y = (run_top + run_height - size);
                        this.addLine(canvas, area, run, provider, line_start, line_y, line_width, size);
                    }
                    
                    if (font.isStrikethrough())
                    {
                        final float line_y = (run_top + (run_height * 0.5f) - size);
                        this.addLine(canvas, area, run, provider, line_start, line_y, line_width, size);
                    }
                }
            }
            
            baseline += (line.descent + (line_height * (line.leading - 1f)));
        }
        
        return client_box;
    }
    
    private void addLine(final Canvas canvas, final Rectangle area, final Run run,
                         final IGradientProvider provider, final float x, final float y, final float width,
                         final float height)
    {
        final float   opacity = canvas.getOpacity();
        final boolean shaded  = run.font.isShaded();
        
        final VertexPalette text_palette;
        VertexPalette shadow_palette = null;
        
        if (run.colour == null)
        {
            text_palette = provider.getPalette(
                (float) area.toRelativeX(x),
                (float) area.toRelativeY(y),
                (float) area.toRelativeX(x + width),
                (float) area.toRelativeY(y + height));
            
            if (shaded)
            {
                shadow_palette = (run.shadowColour == null
                    ? new VertexPalette(
                        ARGB.scaleRGB(text_palette.topLeft(),     0.25f),
                        ARGB.scaleRGB(text_palette.topRight(),    0.25f),
                        ARGB.scaleRGB(text_palette.bottomLeft(),  0.25f),
                        ARGB.scaleRGB(text_palette.bottomRight(), 0.25f))
                    : new VertexPalette((new Colour(run.shadowColour)).withOpacityRel(opacity)));
            }
        }
        else
        {
            final Colour colour = (Colour.fromRgb(run.colour.getValue())).withOpacityRel(opacity);
            text_palette = new VertexPalette(colour);
            
            if (shaded)
            {
                shadow_palette = (run.shadowColour == null
                    ? new VertexPalette(ARGB.scaleRGB(colour.colour(), 0.25f))
                    : new VertexPalette((new Colour(run.shadowColour)).withOpacityRel(opacity)));
            }
        }
        
        canvas.draw(new ICanvasState.Renderable(
            new LineRenderer(x, y, width, height, run.textGlyphs.getFirst().shadowOffset, text_palette, shadow_palette,
                             15728880),
            Optional.ofNullable(canvas.getBrush().getTexture())
                .map(tex -> new TextureSetup(null, tex.getTextureView(), null, null, tex.getSampler(), null))
                .orElseGet(TextureSetup::noTexture),
            null,
            new Rectangle(Mth.floor(x), Mth.floor(y), Mth.ceil(width), Mth.ceil(height))));
    }
    
    //==================================================================================================================
    private void computeLayout(final TextFormat textFormat)
    {
        this.lines.clear();
        
        if (textFormat.isEmpty())
        {
            return;
        }
        
        final WrapHelper wrapper = switch (this.layoutOptions.wordWrap)
        {
            case BREAK_WORD, BREAK_SPACE -> new MultiLineWrapper(
                (StringUtils.countMatches(textFormat.getContent(), '\n') + 1));
            case NO_WRAP -> new SingleLineWrapper(textFormat.getNodeCount());
        };
        
        textFormat.visit(wrapper);
        this.lines.addAll(wrapper.getLines());
        
        this.width  = Mth.ceil(wrapper.getWidth());
        this.height = Mth.ceil(wrapper.getHeight());
    }
}
