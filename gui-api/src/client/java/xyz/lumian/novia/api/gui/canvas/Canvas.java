/**
 * .-----------------. .----------------.  .----------------.  .----------------.  .----------------.
 * | .--------------. || .--------------. || .--------------. || .--------------. || .--------------. |
 * | | ____  _____  | || |     ____     | || | ____   ____  | || |     _____    | || |      __      | |
 * | ||_   \|_   _| | || |   .'    `.   | || ||_  _| |_  _| | || |    |_   _|   | || |     /  \     | |
 * | |  |   \ | |   | || |  /  .--.  \  | || |  \ \   / /   | || |      | |     | || |    / /\ \    | |
 * | |  | |\ \| |   | || |  | |    | |  | || |   \ \ / /    | || |      | |     | || |   / ____ \   | |
 * | | _| |_\   |_  | || |  \  `--'  /  | || |    \ ' /     | || |     _| |_    | || | _/ /    \ \_ | |
 * | ||_____|\____| | || |   `.____.'   | || |     \_/      | || |    |_____|   | || ||____|  |____|| |
 * | |              | || |              | || |              | || |              | || |              | |
 * | '--------------' || '--------------' || '--------------' || '--------------' || '--------------' |
 * '----------------'  '----------------'  '----------------'  '----------------'  '----------------'
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2025 LumiaLights
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package xyz.lumian.novia.api.gui.canvas;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.client.renderer.state.gui.GuiItemRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.gui.GuiMetadataSection;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.client.resources.model.sprite.AtlasManager;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.data.AtlasIds;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import xyz.lumian.novia.api.core.util.Colour;
import xyz.lumian.novia.api.gui.GuiApiId;
import xyz.lumian.novia.api.gui.canvas.brush.Brush;
import xyz.lumian.novia.api.gui.canvas.brush.gradient.Gradient;
import xyz.lumian.novia.api.gui.canvas.renderer.IShapedRender;
import xyz.lumian.novia.api.gui.canvas.renderer.PositionedPanorama;
import xyz.lumian.novia.api.gui.canvas.renderer.ShapedRectangleRender;
import xyz.lumian.novia.api.gui.component.GuiComponent;
import xyz.lumian.novia.api.gui.component.GuiComponentAttorney;
import xyz.lumian.novia.api.gui.component.GuiScreen;
import xyz.lumian.novia.api.gui.component.ScreenLayer;
import xyz.lumian.novia.api.gui.font.GuiFont;
import xyz.lumian.novia.api.gui.font.TextFormat;
import xyz.lumian.novia.api.gui.font.TextLayout;
import xyz.lumian.novia.api.gui.geometry.Alignment;
import xyz.lumian.novia.api.gui.geometry.Point;
import xyz.lumian.novia.api.gui.geometry.Rectangle;
import xyz.lumian.novia.api.gui.geometry.UvMapping;
import xyz.lumian.novia.api.gui.impl.GuiRenderStateAccessor;
import xyz.lumian.novia.api.gui.util.DrawUtil;
import net.minecraft.client.renderer.state.gui.ScreenArea;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;

import java.util.*;
import java.util.function.Function;



//**********************************************************************************************************************
/// A graphics object that sets up the render pipeline for transforming and rendering vertices.
///
/// ## Frames & States
/// Every canvas holds an internal queue of frames, each frame represents a component that is being processed. For each
/// component that is being drawn, the frame will be pushed onto the queue and popped once it finished. Frames hold all
/// the necessary data needed to work with a component, that is its bounds, colour map, transform, template, activity,
/// opacity and font. With the help of these frames it is possible to suspend processing of the component until all
/// children have been processed so it can then be resumed later.
///
/// On top of frames there is also states which build on top of frames and can be used to switch temporary styling
/// information during the processing of a frame. A state contains a momentary transform, a brush, a font, a clipping
/// rectangle and the opacity factor. Each of these can be modified during drawing which will be reverted back to frame
/// defaults upon leaving the drawing routine. They can also be used to temporarily push and pop states multiple times
/// during a single draw call, which allows to keep styling for multiple draw calls without having to set them over and
/// over again (see [Canvas#pushState()], [Canvas#popState()] and [Canvas#runWithState(Runnable)]). Throughout the
/// [Canvas] class there will be methods that are marked as "Stateful", this means that using that function will retain
/// the information given until reset or the frame has finished.
///
/// ## Brushes
/// A nifty feature the Novia GUI framework comes with is brushes ([Brush]). Brushes provide a way to store color and
/// texture information for multiple draw calls, by that means, you set the colour or texture once and the canvas will
/// use this information for future draw calls.
///
/// ## Transforming & Clipping
/// Each frame has its own transform that translates to the screen coordinates of the frame's component, these
/// transforms can be used to translate, rotate and scale future draw calls. Additionally, there is also a clipping
/// region that can be used to limit the area the renderer will eventually draw, positioning of these are dependent on
/// the applied transform of the frame and current state, but they can never exceed out of the frames bounds.
///
/// ## Render-states and Rendering
/// In Minecraft, rendering happens through deferred buffer setups, that is, whenever something in a GUI is calling for
/// rendering, the vertex buffers will be pre-filled with all the necessary information about vertices, such as their
/// texture mapping, light value, colours etc. These are represented through render states (see subclasses of
/// [ScreenArea]), which allow to template what these buffers will be filled with once
/// rendering actually happens. The canvas is building upon this concept similarly to [GuiGraphicsExtractor],
/// but with a lot more abstraction on top. Whenever a component makes a draw call within a canvas object,
/// the object is not going to be rendered directly, but instead it will be pushed to a list. This list will then later
/// be processed during rendering, and every state inside this list then sets up the buffers for actual rendering.
///
/// Novia abstracts these states via the [ICanvasState] class, by automatically applying transforms and picking the
/// right pipeline, based on the arguments given. Furthermore, [IShapedRender] is an abstraction on top of this class
/// that automatically adjusts to brushes, which is why for states involving brushes that these should be preferred
/// (see [Canvas#drawShaped(IShapedRender, Rectangle, RenderPipeline, AbstractTexture)] and [Canvas#draw(ICanvasState)]).
///
public final class Canvas
{
    //******************************************************************************************************************
    private static class State
    {
        //**************************************************************************************************************
        public final AffineTransform transform;

        public Brush     brush;
        public GuiFont   font;
        public Rectangle clipRect;
        public float     opacity;

        //**************************************************************************************************************
        public State(final Rectangle       clipRect,
                     final Brush           brush,
                     final GuiFont         font,
                     final AffineTransform transform,
                     final float           opacity)
        {
            this.clipRect  = clipRect;
            this.transform = transform;
            this.font      = font;
            this.brush     = brush;
            this.opacity   = opacity;
        }

        public State(final State other)
        {
            this(
                new Rectangle(other.clipRect),
                new Brush(other.brush),
                new GuiFont(other.font),
                new AffineTransform(other.transform),
                other.opacity);
        }
    }

    private record Frame(
        Rectangle        clientRect,
        GuiFont          font,
        IGuiTemplate     template,
        IPaletteProvider palette,
        AffineTransform  transform,
        float            opacity,
        boolean          active
    ) {
        //**************************************************************************************************************
        public static Frame forComponent(final GuiComponent component, final Frame parent)
        {
            return new Frame(
                component.getScreenBounds().intersect(parent.clientRect),
                Objects.requireNonNullElse(GuiComponentAttorney.font(component), parent.font),
                Objects.requireNonNullElse(GuiComponentAttorney.template(component), parent.template),
                component,
                (new AffineTransform(parent.transform)).translate(component.getX(), component.getY()),
                (parent.opacity * component.getOpacity()),
                (parent.active && component.hasActiveFlag())
            );
        }

        public static Frame forLayer(final ScreenLayer layer)
        {
            return new Frame(layer.getBounds(), layer.getLayerFont(), layer.getLayerTemplate(), layer.getPalette(),
                             new AffineTransform(), 1.0f, true);
        }

        //==============================================================================================================
        private State createState()
        {
            return new State(new Rectangle(this.clientRect), this.template.getDefaultBrush().get(),
                             new GuiFont(this.font), new AffineTransform(this.transform), this.opacity);
        }
    }

    @FunctionalInterface
    private interface DrawFunc<T>
    {
        //**************************************************************************************************************
        void draw(T text, int x, int y);
    }

    //******************************************************************************************************************
    public static final RenderPipeline.Snippet NOVIA_GUI_SNIPPET;
    public static final RenderPipeline.Snippet NOVIA_POSITION_TEX_COLOR_SNIPPET;

    /// Describes the [RenderPipeline] used to draw shapes with canvases.
    public static final RenderPipeline NOVIA_GUI;

    /// Describes the [RenderPipeline] used to draw textured shapes with canvases.
    public static final RenderPipeline NOVIA_GUI_TEXTURED;
    
    //------------------------------------------------------------------------------------------------------------------
    private static final Identifier INWORLD_MENU_BACKGROUND_TEXTURE;
    
    //==================================================================================================================
    static
    {
        NOVIA_GUI_SNIPPET = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
            .withVertexShader("core/gui")
            .withFragmentShader("core/gui")
		    .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
		    .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
            .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
            .buildSnippet();
        
        NOVIA_POSITION_TEX_COLOR_SNIPPET = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
            .withVertexShader("core/position_tex_color")
            .withFragmentShader("core/position_tex_color")
            .withSampler("Sampler0")
            .withSampler("Sampler1")
		    .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
		    .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
            .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
            .buildSnippet();
        
        NOVIA_GUI = RenderPipelines.register(RenderPipeline.builder(NOVIA_GUI_SNIPPET)
            .withLocation(GuiApiId.RenderPipeline.GUI)
            .build());
        
        NOVIA_GUI_TEXTURED = RenderPipelines.register(RenderPipeline.builder(NOVIA_POSITION_TEX_COLOR_SNIPPET)
            .withLocation(GuiApiId.RenderPipeline.GUI_TEXTURED)
            .build());
        
        INWORLD_MENU_BACKGROUND_TEXTURE = Identifier.withDefaultNamespace("textures/gui/inworld_menu_background.png");
    }

    //******************************************************************************************************************
    /// The absolute screen position of the mouse cursor at the start of the current render pass.
    ///
    /// Tip: Use [Point#toRelativePoint(Point)] with [GuiComponent#getScreenPosition()] to get the mouse
    /// position relative to the current frame's component.
    public final Point mousePos;

    /// The tick delta between each render pass.
    public final float deltaTime;

    //------------------------------------------------------------------------------------------------------------------
    // only used internally for debugging purposes, this is not exposed to the outside otherwise
    final GuiGraphicsExtractor internalContext;

    //------------------------------------------------------------------------------------------------------------------
    private final Deque<Frame>   framebuffer = new ArrayDeque<>(8);
    private final Deque<State>   statebuffer = new ArrayDeque<>(3);
    private final Minecraft      client = Minecraft.getInstance();
    private final TextureAtlas   atlas;
    private final SpriteGetter   sprites;
    private final GuiRenderState renderState;

    private @Nullable State state = null;
    private @Nullable Frame frame = null;

    //******************************************************************************************************************
    public Canvas(final GuiGraphicsExtractor context, final Point mousePos, final float deltaTime)
    {
        this.internalContext = context;
        this.renderState     = Objects.requireNonNull(context,  "context must not be null").guiRenderState;
        this.mousePos        = Objects.requireNonNull(mousePos, "mouse pos must not be null");
        this.deltaTime       = deltaTime;
        
        final AtlasManager atlas = Minecraft.getInstance().getAtlasManager();
        this.atlas   = atlas.getAtlasOrThrow(AtlasIds.GUI);
        this.sprites = atlas;
    }

    //==================================================================================================================
    /// Gets the screen's template.
    ///
    /// Since the returned template is not a copy, it should never be modified in any way, as this might have
    /// unexpected effects on other frames that also inherit this template.
    /// @return The [IGuiTemplate]
    public IGuiTemplate getTemplate() { return Objects.requireNonNull(this.frame).template; }

    /// Gets the current brush. The brush returned is not a copy but the state's own brush, any changes to it will
    /// apply to the current state.
    /// @return The [Brush]
    public Brush getBrush() { return Objects.requireNonNull(this.state).brush; }

    /// Gets the current font. The font returned is not a copy but the state's own font, any changes to it will
    /// apply to the current state.
    /// @return The [GuiFont]
    public GuiFont getFont() { return Objects.requireNonNull(this.state).font; }

    /// Gets the current frame's opacity. The opacity returned is the product of all opacity levels of all parent frames
    /// and the current one, plus the current state's opacity level set with [#setOpacity(float)].
    /// @return The opacity
    public float getOpacity() { return Objects.requireNonNull(this.state).opacity; }

    /// {@return the width of the window scaled to the current GUI scale}
    public int getScaledWindowWidth() { return this.client.getWindow().getGuiScaledWidth(); }

    /// {@return the height of the window scaled to the current GUI scale}
    public int getScaledWindowHeight() { return this.client.getWindow().getGuiScaledHeight(); }

    /// Gets a [Colour] that was specified for the current frame.
    ///
    /// This will first look into the palette of the current frame, if no colour was found it will browse
    /// the current [IGuiTemplate]'s palette and if there was still no colour found,
    /// it will return [Colour#BLACK].
    /// @param id The [ColourId] for the colour
    /// @return The [Colour]
    /// @throws IllegalStateException If no default colour has been registered for the given ID
    public Colour findColour(final ColourId id)
    {
        return Objects.requireNonNull(this.frame).palette
            .getColour(id)
            .or(() -> this.getTemplate().getColour(id))
            .orElse(Colour.BLACK);
    }

    /// Gets a copy of the current state's applied transform.
    ///
    /// Since the returned transform is a copy you cannot directly modify the transform but instead requires you to set
    /// it with either [#setTransform(AffineTransform)] or [#addTransform(AffineTransform)].
    /// @return The [AffineTransform]
    public AffineTransform getTransform() { return new AffineTransform(Objects.requireNonNull(this.state).transform);}

    /// Gets a copy of the currently applied clipping region.
    ///
    /// Since the returned region is a copy you cannot directly modify the region but instead requires you to set
    /// it with either [#setClippingRegion(Rectangle)] or [#setClippingRegion(int, int, int, int)].
    /// @return The current clipping region
    public Rectangle getClippingRegion() { return new Rectangle(Objects.requireNonNull(this.state).clipRect); }
    
    /// Finds a sprite by its given sprite ID (starting in assets folder 'textures/gui/sprites/').
    /// @param spriteId The [SpriteId] of the sprite
    /// @return The [TextureAtlasSprite] for that ID, or [MissingTextureAtlasSprite] if no sprite for that ID was found
    public TextureAtlasSprite findSprite(final SpriteId spriteId) { return this.sprites.get(spriteId); }
    
    /// Gets the scaling algorithm for the given sprite.
    /// @param sprite The [TextureAtlasSprite]
    /// @return The [GuiSpriteScaling] algorithm
    public GuiSpriteScaling getSpriteScaling(final TextureAtlasSprite sprite)
    {
        return (sprite.contents()
            .getAdditionalMetadata(GuiMetadataSection.TYPE)
            .orElse(GuiMetadataSection.DEFAULT))
            .scaling();
    }
    
    /// Gets the scaling algorithm for the given sprite.
    /// @param spriteId The [Identifier] of the sprite
    /// @return The [GuiSpriteScaling] algorithm
    public GuiSpriteScaling getSpriteScaling(final SpriteId spriteId)
    {
        return this.getSpriteScaling(this.findSprite(spriteId));
    }
    
    //==================================================================================================================
    /// Gets whether the current frame is active based on all its parent frames.
    ///
    /// This is useful to avoid [GuiComponent#isActive()] calls, as they need to scan the entire parent hierarchy.
    /// @return `true` if the frame is active
    public boolean isActive() { return Objects.requireNonNull(this.frame).active; }

    //==================================================================================================================
    /// Gets whether the current clip region contains the given absolute coordinates.
    /// @param screenX The absolute left coordinate
    /// @param screenY The absolute y coordinate
    /// @return `true` if the given points lies within the current clipping region
    public boolean clipRegionContains(final int screenX, final int screenY)
    {
        return Objects.requireNonNull(this.state).clipRect.contains(screenX, screenY);
    }

    /// Gets whether the current clip region contains the given absolute coordinates.
    /// @param screenPoint The absolute point
    /// @return `true` if the given points lies within the current clipping region
    public boolean clipRegionContains(final Point screenPoint)
    {
        return screenPoint.transform(this::clipRegionContains);
    }

    /// Gets whether the current clip region intersects with the specified area.
    /// @param screenX The absolute left coordinate of the area
    /// @param screenY The absolute y coordinate of the area
    /// @param width   The width of the area
    /// @param height  The height of the area
    /// @return `true` if the given area intersects with the current clipping region
    public boolean clipRegionIntersects(final int screenX, final int screenY, final int width, final int height)
    {
        return Objects.requireNonNull(this.state).clipRect.intersects(screenX, screenY, width, height);
    }

    /// Gets whether the current clip region intersects with the specified area.
    /// @param screenRect The absolutely positioned area
    /// @return `true` if the given area intersects with the current clipping region
    public boolean clipRegionIntersects(final Rectangle screenRect)
    {
        return screenRect.transform(this::clipRegionIntersects);
    }

    /// Gets whether the current frame's area (the visible component area) contains the given absolute coordinates.
    /// @param screenX The absolute left coordinate
    /// @param screenY The absolute y coordinate
    /// @return `true` if the given points lies within the frame area
    public boolean frameContains(final int screenX, final int screenY)
    {
        return Objects.requireNonNull(this.frame).clientRect.contains(screenX, screenY);
    }

    /// Gets whether the current frame's area (the visible component area) contains the given absolute coordinates.
    /// @param screenPoint The absolute point
    /// @return `true` if the given points lies within the frame area
    public boolean frameContains(final Point screenPoint)
    {
        return screenPoint.transform(this::frameContains);
    }

    /// Gets whether the current frame's area (the visible component area) intersects with the specified area.
    /// @param screenX The absolute left coordinate of the area
    /// @param screenY The absolute y coordinate of the area
    /// @param width   The width of the area
    /// @param height  The height of the area
    /// @return `true` if the given area intersects with the current frame area
    public boolean frameIntersects(final int screenX, final int screenY, final int width, final int height)
    {
        return Objects.requireNonNull(this.frame).clientRect.intersects(screenX, screenY, width, height);
    }

    /// Gets whether the current frame's area (the visible component area) intersects with the specified area.
    /// @param screenRect The absolutely positioned area
    /// @return `true` if the given area intersects with the current frame area
    public boolean frameIntersects(final Rectangle screenRect) { return screenRect.transform(this::frameIntersects); }

    //==================================================================================================================
    /// Sets the brush to a solid colour fill. This operation is stateful (see [Canvas]).
    /// @param colour The new colour
    public void setColour(final int colour) { Objects.requireNonNull(this.state).brush.setColour(colour); }

    /// Sets the brush to a solid colour fill. This operation is stateful (see [Canvas]).
    /// @param colour The new colour
    public void setColour(final Colour colour) { this.setColour(colour.colour()); }

    /// Sets the brush to a gradient colour fill. This operation is stateful (see [Canvas]).
    /// @param gradient The [Gradient]
    public void setGradient(final Gradient gradient) { Objects.requireNonNull(this.state).brush.setGradient(gradient); }
    
    /// Sets the brush to a texture fill. This operation is stateful (see [Canvas]).
    /// @param texture The new [GpuTextureView]
    public void setTexture(final AbstractTexture texture)
    {
        Objects.requireNonNull(this.state).brush.setTexture(texture);
    }
    
    /// Replaces the current brush with the given brush. This operation is stateful (see [Canvas]).
    /// @param brush The new [Brush]
    public void setBrush(final Brush brush) { Objects.requireNonNull(this.state).brush = new Brush(brush); }

    /// Sets the opacity for the canvas. The opacity will be multiplied by the current frame's opacity, by that means,
    /// if the current frame has an opacity of `0.5`, setting the opacity to `0.5` will result in an opacity of `0.25`.
    /// This operation is stateful (see [Canvas]).
    /// @param opacity The opacity level
    public void setOpacity(final float opacity)
    {
        Objects.requireNonNull(this.state).opacity = (opacity * Objects.requireNonNull(this.frame).opacity);
    }

    /// Sets the current state's font used to draw text on screen. This operation is stateful (see [Canvas]).
    /// @param font The new font
    public void setFont(final GuiFont font) { Objects.requireNonNull(this.state).font = new GuiFont(font); }

    /// Sets the new clipping region relative to the origin of the current frame (the component being drawn). The
    /// clipping region cannot go outside the frame's bounds. This operation is stateful (see [Canvas]).
    /// @param rectangle The area to apply the clipping region to
    public void setClippingRegion(final Rectangle rectangle) { rectangle.accept(this::setClippingRegion); }

    /// Sets the new clipping region relative to the origin of the current frame (the component being drawn). The
    /// clipping region cannot go outside the frame's bounds. This operation is stateful (see [Canvas]).
    /// @param x      The start left coordinate of the clipping region
    /// @param y      The start y coordinate of the clipping region
    /// @param width  The width of the clipping region
    /// @param height The height of the clipping region
    public void setClippingRegion(final int x, final int y, final int width, final int height)
    {
        final Rectangle trans_rect = this.getTransform().applyToVertices(x, y, width, height);
        Objects.requireNonNull(this.state).clipRect = Objects.requireNonNull(this.frame).clientRect
            .intersect(trans_rect);
    }

    /// Sets the current transform, allowing rotating, translating and scaling subsequent drawing calls. This operation
    /// is stateful (see [Canvas]).
    /// @param transform The [AffineTransform]
    public void setTransform(final AffineTransform transform)
    {
        final AffineTransform new_trans = new AffineTransform(Objects.requireNonNull(this.frame).transform)
            .multiply(transform);
        Objects.requireNonNull(this.state).transform.set(new_trans);
    }

    /// Adds a transform to the current transform, allowing rotating, translating and scaling subsequent drawing calls.
    /// This operation is stateful (see [Canvas]).
    /// @param transform The [AffineTransform]
    public void addTransform(final AffineTransform transform)
    {
        Objects.requireNonNull(this.state).transform.multiply(transform);
    }

    //==================================================================================================================
    /// Adds a custom render state object to the renderer.
    /// @param renderState The [ICanvasState]
    @SuppressWarnings("resource")
    public void draw(final ICanvasState renderState)
    {
        if (this.getClippingRegion().isEmpty())
        {
            return;
        }
        
        final AffineTransform transform   = Objects.requireNonNull(this.state).transform;
        final ScreenRectangle clip_bounds = this.state.clipRect.toScreenRect();
        final Rectangle       bounds      = Objects.requireNonNullElseGet(
            renderState.bounds(),
            (() -> Objects.requireNonNull(this.frame).clientRect));
        
        final ScreenRectangle rect;
        {
            final Rectangle temp_bounds = transform.applyToVertices(bounds).intersect(clip_bounds);
            
            if (temp_bounds.isEmpty())
            {
                return;
            }
            
            rect = temp_bounds.toScreenRect();
        }
        
        final TextureSetup   texture  = Objects.requireNonNullElseGet(renderState.texture(), TextureSetup::noTexture);
        final RenderPipeline pipeline = Objects.requireNonNullElseGet(
            renderState.pipeline(),
            (() -> ((texture.texure0() != null || texture.texure1() != null)
                ? Canvas.NOVIA_GUI_TEXTURED
                : Canvas.NOVIA_GUI)));
        
        final Matrix3x2f            matrix   = new Matrix3x2f(transform.getMatrix());
        final ICanvasState.Renderer renderer = renderState.renderer();
        
        ((GuiRenderStateAccessor) this.renderState).novia$addState(new GuiElementRenderState()
        {
            //**********************************************************************************************************
            @Override
            public void buildVertices(final VertexConsumer vertices)
            {
                renderer.render(vertices, matrix.translate(0f, 0f));
            }
            
            @Override public RenderPipeline  pipeline()     { return pipeline; }
            @Override public TextureSetup    textureSetup() { return texture; }
            @Override public ScreenRectangle scissorArea()  { return clip_bounds; }
            @Override public ScreenRectangle bounds()       { return rect; }
        });
    }
    
    public void drawShaped(final IShapedRender renderer, final Rectangle bounds)
    {
        this.drawShaped(renderer, bounds, null, null);
    }
    
    public void drawShaped(final IShapedRender renderer, final Rectangle bounds,
                           final @Nullable AbstractTexture texture)
    {
        this.drawShaped(renderer, bounds, null, texture);
    }
    
    public void drawShaped(final IShapedRender renderer, final Rectangle bounds,
                           final @Nullable RenderPipeline pipeline, final @Nullable AbstractTexture texture)
    {
        final AbstractTexture brush_texture = this.getBrush().getTexture();
        final TextureSetup    setup         = new TextureSetup(
            (texture != null ? texture.getTextureView() : null),
            (brush_texture != null ? brush_texture.getTextureView() : null),
            null,
            (texture != null ? texture.getSampler() : null),
            (brush_texture != null ? brush_texture.getSampler() : null),
            null);
        
        this.getBrush().apply(renderer, this.getOpacity());
        this.draw(new ICanvasState.Renderable(renderer, setup, pipeline, bounds));
    }
    
    public void draw(final GuiElementRenderState renderState)
    {
        if (this.getClippingRegion().isEmpty() || renderState.scissorArea() == null || renderState.bounds() == null)
        {
            return;
        }
        
        ((GuiRenderStateAccessor) this.renderState).novia$addState(renderState);
    }

    public void drawItem(final GuiItemRenderState state) { this.renderState.addItem(state); }

    //==================================================================================================================
    /// Draws a horizontal line.
    /// @param x      The left position of the line
    /// @param y      The top y position of the line
    /// @param length The length of the line (width)
    /// @see Canvas#setBrush(Brush)
    /// @see Canvas#getBrush()
    /// @see Brush
    public void drawHorizontalLine(final int x, final int y, final int length) { this.fill(x, y, length, 1); }

    /// Draws a horizontal line.
    /// @param pos    The top-left start position of the line
    /// @param length The length of the line (width)
    /// @see Canvas#setBrush(Brush)
    /// @see Canvas#getBrush()
    /// @see Brush
    public void drawHorizontalLine(final Point pos, final int length)
    {
        pos.accept((x, y) -> this.drawHorizontalLine(x, y, length));
    }

    /// Draws a vertical line.
    /// @param x      The left position of the line
    /// @param y      The top y position of the line
    /// @param length The length of the line (height)
    /// @see Canvas#setBrush(Brush)
    /// @see Canvas#getBrush()
    /// @see Brush
    public void drawVerticalLine(final int x, final int y, final int length) { this.fill(x, y, 1, length); }

    /// Draws a vertical line.
    /// @param pos    The top-left start position of the line
    /// @param length The length of the line (width)
    /// @see Canvas#setBrush(Brush)
    /// @see Canvas#getBrush()
    /// @see Brush
    public void drawVerticalLine(final Point pos, final int length)
    {
        pos.accept((x, y) -> this.drawVerticalLine(x, y, length));
    }

    //==================================================================================================================
    /// Draws a transparent black texture on top, giving the sensation of making everything behind it darker.
    /// @param x      The left position of the darkened area
    /// @param y      The y position of the darkened area
    /// @param width  The width of the darkened area
    /// @param height The height of the darkened area
    /// @see Canvas#setBrush(Brush)
    /// @see Canvas#getBrush()
    /// @see Brush
    public void drawDarkening(final int x, final int y, final int width, final int height)
    {
        final Identifier texture = (this.client.level == null
            ? Screen.MENU_BACKGROUND
            : Canvas.INWORLD_MENU_BACKGROUND_TEXTURE);
        this.drawTexture(texture, x, y, width, height, false);
    }

    /// Draws a transparent black texture on top, giving the sensation of making everything behind it darker.
    /// @param rect The area to darken
    /// @see Canvas#setBrush(Brush)
    /// @see Canvas#getBrush()
    /// @see Brush
    public void drawDarkening(final Rectangle rect) { rect.accept(this::drawDarkening); }

    /// Draws the iconic Minecraft main menu panorama in the specified area.
    /// @param x      The left position of the panorama
    /// @param y      The y position of the panorama
    /// @param width  The width of the panorama
    /// @param height The height of the panorama
    /// @param rotate Whether the panorama should rotate
    /// @see Canvas#setBrush(Brush)
    /// @see Canvas#getBrush()
    /// @see Brush
    public void drawPanorama(final int x, final int y, final int width, final int height, final boolean rotate)
    {
        PositionedPanorama.INSTANCE.render(this, x, y, width, height, rotate);
    }

    /// Draws the iconic Minecraft main menu panorama in the specified area.
    /// @param rect   The area to draw the panorama in
    /// @param rotate Whether the panorama should rotate
    /// @see Canvas#setBrush(Brush)
    /// @see Canvas#getBrush()
    /// @see Brush
    public void drawPanorama(final Rectangle rect, final boolean rotate)
    {
        rect.accept((x, y, w, h) -> this.drawPanorama(x, y, w, h, rotate));
    }

    //==================================================================================================================
    /// Fills a rectangle with the currently set brush.
    /// @param rect The area to fill
    /// @see Canvas#setBrush(Brush)
    /// @see Canvas#getBrush()
    /// @see Brush
    public void fill(final Rectangle rect) { this.drawShaped(new ShapedRectangleRender.Fill(rect), rect); }

    /// Fills a rectangle with the currently set brush.
    /// @param x      The left position of the rectangle
    /// @param y      The y position of the rectangle
    /// @param width  The width of the rectangle
    /// @param height The height of the rectangle
    /// @see Canvas#setBrush(Brush)
    /// @see Canvas#getBrush()
    /// @see Brush
    public void fill(final int x, final int y, final int width, final int height)
    {
        this.fill(new Rectangle(x, y, width, height));
    }
    
    /** Fills the entire's component area with the currently set brush. */
    public void fill() { this.fill(Objects.requireNonNull(this.state).clipRect); }

    /// Draws a rectangular outline.
    /// @param rect The area to draw a border around
    /// @see Canvas#setBrush(Brush)
    /// @see Canvas#getBrush()
    /// @see Brush
    public void drawRect(final Rectangle rect)
    {
        this.drawShaped(new ShapedRectangleRender.Border(rect, 1f), rect);
    }
    
    /// Draws a rectangular outline.
    /// @param x      The left position of the rectangle
    /// @param y      The y position of the rectangle
    /// @param width  The width of the rectangle
    /// @param height The height of the rectangle
    /// @see Canvas#setBrush(Brush)
    /// @see Canvas#getBrush()
    /// @see Brush
    public void drawRect(final int x, final int y, final int width, final int height)
    {
        this.drawRect(new Rectangle(x, y, width, height));
    }
    
    /// Draws a border around the entire component's area with the currently set brush.
    public void drawRect() { this.drawRect(Objects.requireNonNull(this.state).clipRect); }

    //==================================================================================================================
    /// Draws the given text on screen with the current [GuiFont].
    /// @param text The string to draw
    /// @param x    The draw coordinate on the left-axis
    /// @param y    The draw coordinate on the y-axis
    /// @see Canvas#setBrush(Brush)
    /// @see Canvas#getBrush()
    /// @see Brush
    public void drawText(final String text, final int x, final int y)
    {
        final TextFormat format = new TextFormat();
        format.append(text, this.getFont());
        format.draw(this, x, y);
    }

    /// Draws the given text on screen with the current [GuiFont].
    /// @param text The [Component] component to draw
    /// @param x    The draw coordinate on the left-axis
    /// @param y    The draw coordinate on the y-axis
    /// @see Canvas#setBrush(Brush)
    /// @see Canvas#getBrush()
    /// @see Brush
    public void drawText(final Component text, final int x, final int y)
    {
        final TextFormat format = new TextFormat();
        format.append(text, this.getFont());
        format.draw(this, x, y);
    }

    /// Draws the given text on screen with the current [GuiFont].
    /// @param text The [FormattedCharSequence] to draw
    /// @param x    The top-left coordinate on the left-axis
    /// @param y    The top-left coordinate on the y-axis
    /// @see Canvas#setBrush(Brush)
    /// @see Canvas#getBrush()
    /// @see Brush
    public void drawText(final FormattedCharSequence text, final int x, final int y)
    {
        final TextFormat format = new TextFormat();
        format.append(text, this.getFont());
        format.draw(this, x, y);
    }

    /// Draws the given text on screen with the current [GuiFont].
    /// @param text  The string to draw
    /// @param point The top-left point
    /// @see Canvas#setBrush(Brush)
    /// @see Canvas#getBrush()
    /// @see Brush
    public void drawText(final String text, final Point point)
    {
        point.accept((x, y) -> this.drawText(text, x, y));
    }

    /// Draws the given text on screen with the current [GuiFont].
    /// @param text  The [Component] component to draw
    /// @param point The top-left point
    /// @see Canvas#setBrush(Brush)
    /// @see Canvas#getBrush()
    /// @see Brush
    public void drawText(final Component text, final Point point)
    {
        point.accept((x, y) -> this.drawText(text, x, y));
    }

    /// Draws the given text on screen with the current [GuiFont].
    /// @param text  The [FormattedCharSequence] to draw
    /// @param point The top-left point
    /// @see Canvas#setBrush(Brush)
    /// @see Canvas#getBrush()
    /// @see Brush
    public void drawText(final FormattedCharSequence text, final Point point)
    {
        point.accept((x, y) -> this.drawText(text, x, y));
    }
    
    //==================================================================================================================
    /// Draws the given text on screen with the current [GuiFont] and aligned to the given region.
    /// @param text      The text to draw
    /// @param x         The left coordinate of the alignment area
    /// @param y         The y coordinate of the alignment area
    /// @param width     The width the alignment area
    /// @param height    The height of the alignment area
    /// @param alignment The [Alignment] to use to align the text
    /// @see Canvas#setBrush(Brush)
    /// @see Canvas#getBrush()
    /// @see Brush
    public void drawTextAligned(final String text,
                                final int x, final int y, final int width, final int height,
                                final Alignment alignment)
    {
        this.drawTextAligned((new TextFormat()).append(text, this.getFont()),
                             alignment,
                             new Rectangle(x, y, width, height));
    }

    /// Draws the given text on screen with the current [GuiFont] and aligned to the given region.
    /// @param text      The text to draw
    /// @param x         The left coordinate of the alignment area
    /// @param y         The y coordinate of the alignment area
    /// @param width     The width the alignment area
    /// @param height    The height of the alignment area
    /// @param alignment The [Alignment] to use to align the text
    /// @see Canvas#setBrush(Brush)
    /// @see Canvas#getBrush()
    /// @see Brush
    public void drawTextAligned(final Component text,
                                final int x, final int y, final int width, final int height,
                                final Alignment alignment)
    {
        this.drawTextAligned((new TextFormat()).append(text, this.getFont()),
                             alignment,
                             new Rectangle(x, y, width, height));
    }

    /// Draws the given text on screen with the current [GuiFont] and aligned to the given region.
    /// @param text      The text to draw
    /// @param x         The left coordinate of the alignment area
    /// @param y         The y coordinate of the alignment area
    /// @param width     The width the alignment area
    /// @param height    The height of the alignment area
    /// @param alignment The [Alignment] to use to align the text
    /// @see Canvas#setBrush(Brush)
    /// @see Canvas#getBrush()
    /// @see Brush
    public void drawTextAligned(final FormattedCharSequence text,
                                final int x, final int y, final int width, final int height,
                                final Alignment alignment)
    {
        this.drawTextAligned((new TextFormat()).append(text, this.getFont()),
                             alignment,
                             new Rectangle(x, y, width, height));
    }

    /// Draws the given text on screen with the current [GuiFont] and aligned to the given region.
    /// @param text      The text to draw
    /// @param area      The [Rectangle] to align the text with
    /// @param alignment The [Alignment] to use to align the text
    /// @see Canvas#setBrush(Brush)
    /// @see Canvas#getBrush()
    /// @see Brush
    public void drawTextAligned(final String text, final Rectangle area, final Alignment alignment)
    {
        this.drawTextAligned((new TextFormat()).append(text, this.getFont()), alignment, area);
    }

    /// Draws the given text on screen with the current [GuiFont] and aligned to the given region.
    /// @param text      The text to draw
    /// @param area      The [Rectangle] to align the text with
    /// @param alignment The [Alignment] to use to align the text
    /// @see Canvas#setBrush(Brush)
    /// @see Canvas#getBrush()
    /// @see Brush
    public void drawTextAligned(final Component text, final Rectangle area, final Alignment alignment)
    {
        this.drawTextAligned((new TextFormat()).append(text, this.getFont()), alignment, area);
    }

    /// Draws the given text on screen with the current [GuiFont] and aligned to the given region.
    /// @param text      The text to draw
    /// @param area      The [Rectangle] to align the text with
    /// @param alignment The [Alignment] to use to align the text
    /// @see Canvas#setBrush(Brush)
    /// @see Canvas#getBrush()
    /// @see Brush
    public void drawTextAligned(final FormattedCharSequence text, final Rectangle area, final Alignment alignment)
    {
        this.drawTextAligned((new TextFormat()).append(text, this.getFont()), alignment, area);
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private void drawTextAligned(final TextFormat format, final Alignment alignment, final Rectangle bounds)
    {
        final TextLayout.LayoutOptions options = new TextLayout.LayoutOptions();
        options.alignment = alignment;
        options.wordWrap  = TextLayout.WordWrap.NO_WRAP;
        
        final TextLayout layout = new TextLayout(format, options);
        layout.draw(this, bounds);
    }
    
    //==================================================================================================================
    public void drawScrollableText(final String text, final int x, final int y, final int width, final int height)
    {
        this.drawScrollableText(text, x, y, width, height, ((x * 2 + width) / 2));
    }

    public void drawScrollableText(final Component text, final int x, final int y, final int width, final int height)
    {
        this.drawScrollableText(text, x, y, width, height, ((x * 2 + width) / 2));
    }

    public void drawScrollableText(final FormattedCharSequence text,
                                   final int x, final int y, final int width, final int height)
    {
        this.drawScrollableText(text, x, y, width, height, ((x * 2 + width) / 2));
    }

    public void drawScrollableText(final String text,
                                   final int x, final int y, final int width, final int height, final int centerX)
    {
        this.drawScrollableText(this::drawText, this.getFont()::getTextWidthFitted, text, x, y, width, height, centerX);
    }

    public void drawScrollableText(final Component text,
                                   final int x, final int y, final int width, final int height, final int centerX)
    {
        this.drawScrollableText(this::drawText, (text1 -> TextLayout.getTextWidthFitted(this.getFont(), text1)), text,
                                x, y, width, height, centerX);
    }

    public void drawScrollableText(final FormattedCharSequence text,
                                   final int x, final int y, final int width, final int height, final int centerX)
    {
        this.drawScrollableText(this::drawText, (text1 -> TextLayout.getTextWidthFitted(this.getFont(), text1)), text,
                                x, y, width, height, centerX);
    }

    public void drawScrollableText(final String text, final Rectangle rect)
    {
        rect.accept((x, y, w, h) -> this.drawScrollableText(text, x, y, w, h));
    }

    public void drawScrollableText(final Component text, final Rectangle rect)
    {
        rect.accept((x, y, w, h) -> this.drawScrollableText(text, x, y, w, h));
    }

    public void drawScrollableText(final FormattedCharSequence text, final Rectangle rect)
    {
        rect.accept((x, y, w, h) -> this.drawScrollableText(text, x, y, w, h));
    }

    public void drawScrollableText(final String text, final Rectangle rect, final int centerX)
    {
        rect.accept((x, y, w, h) -> this.drawScrollableText(text, x, y, w, h, centerX));
    }

    public void drawScrollableText(final Component text, final Rectangle rect, final int centerX)
    {
        rect.accept((x, y, w, h) -> this.drawScrollableText(text, x, y, w, h, centerX));
    }

    public void drawScrollableText(final FormattedCharSequence text, final Rectangle rect, final int centerX)
    {
        rect.accept((x, y, w, h) -> this.drawScrollableText(text, x, y, w, h, centerX));
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private <T> void drawScrollableText(final DrawFunc<T> drawFunc, final Function<T, Integer> widthFunc, final T text,
                                        final int x, final int y, final int width, final int height, final int centerX)
    {
        final int text_width = widthFunc.apply(text);
        final int text_y = ((int) (((y * 2 + height) - this.client.font.lineHeight) * 0.5));
        final int text_right = (x + width);
        final int draw_width = (text_right - x);

        if (text_width > draw_width) {
            final int overflow = (text_width - width + 1);
            final double time = (Util.getMillis() / 1000.0);
            final double e = Math.max(overflow * 0.5, 3.0);
            final double f = (Math.sin((Math.PI * 0.5) * Math.cos(Math.TAU * time / e)) * 0.5 + 0.5);
            final int shift = (int) Mth.lerp(f, 0.0, overflow);

            this.runWithState(() ->
            {
                this.setClippingRegion(x, y, width, height);
                drawFunc.draw(text, (x - shift), text_y);
            });
        } else {
            final int text_c = (text_width / 2);
            final int text_x = (Mth.clamp(centerX, (x + text_c), (text_right - text_c)) - text_c);

            drawFunc.draw(text, text_x, text_y);
        }
    }

    //==================================================================================================================
    /// Draws a region of a sprite on the screen with the scaling being automatically determined by the texture atlas.
    /// @param spriteId The id of the texture inside the assets' sprites folder ("textures/gui/sprites")
    /// @param x        The left coordinate
    /// @param y        The y coordinate
    /// @param width    The width of the area to draw
    /// @param height   The height of the area to draw
    /// @param uv       The [UvMapping] texture region
    /// @param useBrush Whether to use the currently set brush to apply to the texture
    public void drawSprite(final Identifier spriteId,
                           final int x, final int y, final int width, final int height,
                           final UvMapping uv, final boolean useBrush)
    {
        final TextureAtlasSprite sprite  = this.atlas.getSprite(spriteId);
        final GuiSpriteScaling   scaling = this.getSpriteScaling(sprite);
        this.drawSprite(sprite, scaling, x, y, width, height, uv, useBrush);
    }
    
    /// Draws a region of a sprite on the screen with the scaling being automatically determined by the texture atlas.
    /// @param spriteId The id of the texture inside the assets' sprites folder ("textures/gui/sprites")
    /// @param rect     The [Rectangle] to draw the texture in
    /// @param uv       The [UvMapping] texture region
    /// @param useBrush Whether to use the currently set brush to apply to the texture
    public void drawSprite(final Identifier spriteId, final Rectangle rect, final UvMapping uv, final boolean useBrush)
    {
        rect.accept((x, y, w, h) -> this.drawSprite(spriteId, x, y, w, h, uv, useBrush));
    }
    
    /// Draws a sprite on the screen with the scaling being automatically determined by the texture atlas.
    /// @param spriteId The id of the texture inside the assets' sprites folder ("textures/gui/sprites")
    /// @param x        The left coordinate
    /// @param y        The y coordinate
    /// @param width    The width of the area to draw
    /// @param height   The height of the area to draw
    /// @param useBrush Whether to use the currently set brush to apply to the texture
    public void drawSprite(final Identifier spriteId,
                           final int x, final int y, final int width, final int height, final boolean useBrush)
    {
        this.drawSprite(spriteId, x, y, width, height, UvMapping.FULL, useBrush);
    }
    
    /// Draws a sprite on the screen with the scaling being automatically determined by the texture atlas.
    /// @param spriteId The id of the texture inside the assets' sprites folder ("textures/gui/sprites")
    /// @param rect     The [Rectangle] to draw the texture in
    /// @param useBrush Whether to use the currently set brush to apply to the texture
    public void drawSprite(final Identifier spriteId, final Rectangle rect, final boolean useBrush)
    {
        rect.accept((x, y, w, h) -> this.drawSprite(spriteId, x, y, w, h, useBrush));
    }
    
    /// Draws a given region of a sprite with the given scaling algorithm.
    /// @param sprite   The sprite to draw (can be fetched from the texture atlas)
    /// @param scaling  The scaling algorithm
    /// @param x        The left coordinate
    /// @param y        The y coordinate
    /// @param width    The width of the area to draw
    /// @param height   The height of the area to draw
    /// @param uv       The [UvMapping] texture region
    /// @param useBrush Whether to use the currently set brush to apply to the texture
    public void drawSprite(final TextureAtlasSprite sprite, final GuiSpriteScaling scaling,
                           final int x, final int y, final int width, final int height,
                           final UvMapping uv,
                           final boolean useBrush)
    {
        switch (scaling)
        {
            case GuiSpriteScaling.Stretch   s -> this.drawSpriteStretched (sprite,    x, y, width, height, uv, useBrush);
            case GuiSpriteScaling.NineSlice n -> this.drawSpriteNineSliced(sprite, n, x, y, width, height, uv, useBrush);
            case GuiSpriteScaling.Tile      t -> this.drawSpriteTiled     (sprite, t, x, y, width, height, uv, useBrush);

            default -> throw new IllegalStateException("Unexpected scaling: " + scaling);
        }
    }
    
    /// Draws a sprite with the given scaling algorithm.
    /// @param sprite   The sprite to draw (can be fetched from the texture atlas)
    /// @param scaling  The scaling algorithm
    /// @param rect     The [Rectangle] to draw the texture in
    /// @param useBrush Whether to use the currently set brush to apply to the texture
    public void drawSprite(final TextureAtlasSprite sprite, final GuiSpriteScaling scaling,
                           final Rectangle rect, final UvMapping uv, final boolean useBrush)
    {
        rect.accept((x, y, w, h) -> this.drawSprite(sprite, scaling, x, y, w, h, uv, useBrush));
    }
    
    /// Draws a sprite with the given scaling algorithm.
    /// @param sprite   The sprite to draw (can be fetched from the texture atlas)
    /// @param scaling  The scaling algorithm
    /// @param x        The left coordinate
    /// @param y        The y coordinate
    /// @param width    The width of the area to draw
    /// @param height   The height of the area to draw
    /// @param useBrush Whether to use the currently set brush to apply to the texture
    public void drawSprite(final TextureAtlasSprite sprite, final GuiSpriteScaling scaling,
                           final int x, final int y, final int width, final int height,
                           final boolean useBrush)
    {
        this.drawSprite(sprite, scaling, x, y, width, height, UvMapping.FULL, useBrush);
    }
    
    /// Draws a sprite with the given scaling algorithm.
    /// @param sprite   The sprite to draw (can be fetched from the texture atlas)
    /// @param scaling  The scaling algorithm
    /// @param rect     The [Rectangle] to draw the texture in
    /// @param useBrush Whether to use the currently set brush to apply to the texture
    public void drawSprite(final TextureAtlasSprite sprite, final GuiSpriteScaling scaling,
                           final Rectangle rect, final boolean useBrush)
    {
        rect.accept((x, y, w, h) -> this.drawSprite(sprite, scaling, x, y, w, h, UvMapping.FULL, useBrush));
    }
    
    /// Draws a given region of a sprite stretched to the given area.
    /// @param sprite   The sprite to draw (can be fetched from the texture atlas)
    /// @param x        The left coordinate
    /// @param y        The y coordinate
    /// @param width    The width of the area to draw
    /// @param height   The height of the area to draw
    /// @param uv       The [UvMapping] texture region
    /// @param useBrush Whether to use the currently set brush to apply to the texture
    public void drawSpriteStretched(final TextureAtlasSprite sprite,
                                    final int x, final int y, final int width, final int height,
                                    final UvMapping uv, final boolean useBrush)
    {
        if (width > 0 && height > 0)
        {
            this.drawTexturedQuadSprite(sprite, x, y, (x + width), (y + height), uv.minU(), uv.minV(), uv.maxU(),
                                        uv.maxV(), useBrush);
        }
    }
    
    /// Draws a given region of a sprite stretched to the given area.
    /// @param sprite   The sprite to draw (can be fetched from the texture atlas)
    /// @param rect     The [Rectangle] to draw the texture in
    /// @param uv       The [UvMapping] texture region
    /// @param useBrush Whether to use the currently set brush to apply to the texture
    public void drawSpriteStretched(final TextureAtlasSprite sprite, final Rectangle rect, final UvMapping uv,
                                    final boolean useBrush)
    {
        rect.accept((x, y, w, h) -> this.drawSpriteStretched(sprite, x, y, w, h, uv, useBrush));
    }
    
    /// Draws an entire sprite stretched to the given area.
    /// @param sprite   The sprite to draw (can be fetched from the texture atlas)
    /// @param x        The left coordinate
    /// @param y        The y coordinate
    /// @param width    The width of the area to draw
    /// @param height   The height of the area to draw
    /// @param useBrush Whether to use the currently set brush to apply to the texture
    public void drawSpriteStretched(final TextureAtlasSprite sprite,
                                    final int x, final int y, final int width, final int height, final boolean useBrush)
    {
        this.drawSpriteStretched(sprite, x, y, width, height, UvMapping.FULL, useBrush);
    }
    
    /// Draws an entire sprite stretched to the given area.
    /// @param sprite   The sprite to draw (can be fetched from the texture atlas)
    /// @param rect     The [Rectangle] to draw the texture in
    /// @param useBrush Whether to use the currently set brush to apply to the texture
    public void drawSpriteStretched(final TextureAtlasSprite sprite, final Rectangle rect, final boolean useBrush)
    {
        rect.accept((x, y, w, h) -> this.drawSpriteStretched(sprite, x, y, w, h, useBrush));
    }

    /// Draws a region of a sprite on the screen that is tiled and repeated to fill its given area.
    /// @param sprite   The sprite to draw (can be fetched from the texture atlas)
    /// @param scaling  The [GuiSpriteScaling.Tile] scaling
    /// @param x        The left coordinate
    /// @param y        The y coordinate
    /// @param width    The width of the area to draw
    /// @param height   The height of the area to draw
    /// @param uv       The [UvMapping] texture region to use as tile, this is the part that will be repeated
    /// @param useBrush Whether to use the currently set brush to apply to the texture
    public void drawSpriteTiled(final TextureAtlasSprite sprite, final GuiSpriteScaling.Tile scaling,
                                final int x, final int y, final int width, final int height,
                                final UvMapping uv, final boolean useBrush)
    {
        if (width <= 0 || height <= 0)
        {
            return;
        }

        final int tile_u      = (int) (scaling.width()   * uv.minU());
        final int tile_v      = (int) (scaling.height()  * uv.minV());
        final int tile_width  = (int) ((scaling.width()  * uv.maxU()) - tile_u);
        final int tile_height = (int) ((scaling.height() * uv.maxV()) - tile_v);

        this.drawSpriteTiled(sprite, x, y, width, height, tile_u, tile_v, tile_width, tile_height, tile_width,
                             tile_height, useBrush);
    }
    
    /// Draws a region of a sprite on the screen that is tiled and repeated to fill its given area.
    /// @param sprite   The sprite to draw (can be fetched from the texture atlas)
    /// @param scaling  The [GuiSpriteScaling.Tile] scaling
    /// @param rect     The [Rectangle] to draw the texture in
    /// @param uv       The [UvMapping] texture region to use as tile, this is the part that will be repeated
    /// @param useBrush Whether to use the currently set brush to apply to the texture
    public void drawSpriteTiled(final TextureAtlasSprite sprite, final GuiSpriteScaling.Tile scaling,
                                final Rectangle rect, final UvMapping uv, final boolean useBrush)
    {
        rect.accept((x, y, w, h) -> this.drawSpriteTiled(sprite, scaling, x, y, w, h, uv, useBrush));
    }
    
    /// Draws a sprite on the screen that is tiled and repeated to fill its given area.
    /// @param sprite   The sprite to draw (can be fetched from the texture atlas)
    /// @param scaling  The [GuiSpriteScaling.Tile] scaling
    /// @param x        The left coordinate
    /// @param y        The y coordinate
    /// @param width    The width of the area to draw
    /// @param height   The height of the area to draw
    /// @param useBrush Whether to use the currently set brush to apply to the texture
    public void drawSpriteTiled(final TextureAtlasSprite sprite, final GuiSpriteScaling.Tile scaling,
                                final int x, final int y, final int width, final int height, final boolean useBrush)
    {
        this.drawSpriteTiled(sprite, x, y, width, height, 0, 0, scaling.width(), scaling.height(), scaling.width(),
                             scaling.height(), useBrush);
    }
    
    /// Draws a sprite on the screen that is tiled and repeated to fill its given area.
    /// @param sprite   The sprite to draw (can be fetched from the texture atlas)
    /// @param scaling  The [GuiSpriteScaling.Tile] scaling
    /// @param rect     The [Rectangle] to draw the texture in
    /// @param useBrush Whether to use the currently set brush to apply to the texture
    public void drawSpriteTiled(final TextureAtlasSprite sprite, final GuiSpriteScaling.Tile scaling,
                                final Rectangle rect, final boolean useBrush)
    {
        rect.accept((x, y, w, h) -> this.drawSpriteTiled(sprite, scaling, x, y, w, h, useBrush));
    }
    
    /// Draws a region of a sprite on the screen that is 9-sliced
    /// (see [9-slice scaling](https://en.wikipedia.org/wiki/9-slice_scaling)).
    /// @param sprite   The sprite to draw (can be fetched from the texture atlas)
    /// @param scaling  The [GuiSpriteScaling.NineSlice] scaling
    /// @param x        The left coordinate
    /// @param y        The y coordinate
    /// @param width    The width of the area to draw
    /// @param height   The height of the area to draw
    /// @param uv       The [UvMapping] texture region to use as 9-slice, this is the part that will be repeated
    /// @param useBrush Whether to use the currently set brush to apply to the texture
    public void drawSpriteNineSliced(final TextureAtlasSprite sprite, final GuiSpriteScaling.NineSlice scaling,
                                     final int x, final int y, final int width, final int height,
                                     final UvMapping uv, final boolean useBrush)
    {
        final GuiSpriteScaling.NineSlice.Border border = scaling.border();
        final int                               left   = Math.min(border.left(),   (width  / 2));
        final int                               top    = Math.min(border.top(),    (height / 2));
        final int                               right  = Math.min(border.right(),  (width  / 2));
        final int                               bottom = Math.min(border.bottom(), (height / 2));
        
        final int texture_u      = (int) (scaling.width()  * uv.minU());
        final int texture_v      = (int) (scaling.height() * uv.minV());
        final int texture_width  = (int) (scaling.width()  * uv.maxU() - texture_u);
        final int texture_height = (int) (scaling.height() * uv.maxV() - texture_v);
        
        if (width == texture_width && height == texture_height)
        {
            // Full sprite
            this.drawSpriteRegion(sprite, x, y, width, height, texture_u, texture_v, texture_width, texture_height,
                                  useBrush);
        }
        else if (height == texture_height)
        {
            final int right_edge = (texture_width - right);
            
            // Left edge
            this.drawSpriteRegion(sprite, x, y, left, height, texture_u, texture_v, texture_width, texture_height,
                                  useBrush);
            
            // Middle edge
            this.drawInnerSprite(scaling, sprite, (x + left), y, (width - right - left), height, (texture_u + left),
                                 texture_v, texture_width, texture_height, (right_edge - left), texture_height,
                                 useBrush);

            // Right edge
            this.drawSpriteRegion(sprite, (x + width - right), y, right, height, (texture_u + right_edge), texture_v,
                                  texture_width, texture_height, useBrush);
        }
        else if (width == texture_width)
        {
            final int bottom_edge = (texture_height - bottom);
            
            // Top edge
            this.drawSpriteRegion(sprite, x, y, width, top, texture_u, texture_v, texture_width, texture_height,
                                  useBrush);

            // Middle edge
            this.drawInnerSprite(scaling, sprite, x, (y + top), width, (height - bottom - top), texture_u,
                                 (texture_v + top), texture_width, texture_height, texture_width, (bottom_edge - top),
                                 useBrush);

            // Bottom edge
            this.drawSpriteRegion(sprite, x, (y + height - bottom), width, bottom, texture_u, (texture_v + bottom_edge),
                                  texture_width, texture_height, useBrush);
        }
        else
        {
            final int draw_x      = (x + left);
            final int draw_x2     = (x + width - right);
            final int draw_y      = (y + top);
            final int draw_width  = (width - right - left);
            final int draw_height = (height - bottom - top);
            final int tile_width  = (texture_width - right - left);
            final int tile_height = (texture_height - bottom - top);
            final int text_u      = (y + height - bottom);
            final int text_u2     = (texture_width - right);
            final int text_v      = (texture_height - bottom);

            // Top-left corner
            this.drawSpriteRegion(sprite, x, y, left, top, texture_u, texture_v, texture_width, texture_height,
                                  useBrush);

            // Top edge
            this.drawInnerSprite(scaling, sprite, draw_x, y, draw_width, top, (texture_u + left), texture_v,
                                 texture_width, texture_height, tile_width, top, useBrush);

            // Top-right corner
            this.drawSpriteRegion(sprite, draw_x2, y, right, top, (texture_u + text_u2), texture_v, texture_width,
                                  texture_height, useBrush);

            // Bottom-left corner
            this.drawSpriteRegion(sprite, x, text_u, left, bottom, texture_u, (texture_v + text_v), texture_width,
                                  texture_height, useBrush);

            // Bottom edge
            this.drawInnerSprite(scaling, sprite, draw_x, text_u, draw_width, bottom, (texture_u + left),
                                 (texture_v + text_v), texture_width, texture_height, tile_width, bottom, useBrush);

            // Bottom-right corner
            this.drawSpriteRegion(sprite, draw_x2, text_u, right, bottom, (texture_u + text_u2), (texture_v + text_v),
                                  texture_width, texture_height, useBrush);

            // Left edge
            this.drawInnerSprite(scaling, sprite, x, draw_y, left, draw_height, texture_u, (texture_v + top),
                                 texture_width, texture_height, left, tile_height, useBrush);

            // Center
            this.drawInnerSprite(scaling, sprite, draw_x, draw_y, draw_width, draw_height, (texture_u + left),
                                 (texture_v + top), texture_width, texture_height, tile_width, tile_height, useBrush);

            // Right edge
            this.drawInnerSprite(scaling, sprite, draw_x2, draw_y, right, draw_height, (texture_u + text_u2),
                                 (texture_v + top), texture_width, texture_height, right, tile_height, useBrush);
        }
    }
    
    /// Draws a region of a sprite on the screen that is 9-sliced
    /// (see [9-slice scaling](https://en.wikipedia.org/wiki/9-slice_scaling)).
    /// @param sprite   The sprite to draw (can be fetched from the texture atlas)
    /// @param scaling  The [GuiSpriteScaling.NineSlice] scaling
    /// @param rect     The [Rectangle] to draw the texture in
    /// @param uv       The [UvMapping] texture region to use as 9-slice, this is the part that will be repeated
    /// @param useBrush Whether to use the currently set brush to apply to the texture
    public void drawSpriteNineSliced(final TextureAtlasSprite sprite, final GuiSpriteScaling.NineSlice scaling,
                                     final Rectangle rect, final UvMapping uv, final boolean useBrush)
    {
        rect.accept((x, y, w, h) -> this.drawSpriteNineSliced(sprite, scaling, x, y, w, h, uv, useBrush));
    }
    
    /// Draws a sprite on the screen that is 9-sliced
    /// (see [9-slice scaling](https://en.wikipedia.org/wiki/9-slice_scaling)).
    /// @param sprite   The sprite to draw (can be fetched from the texture atlas)
    /// @param scaling  The [GuiSpriteScaling.NineSlice] scaling
    /// @param x        The left coordinate
    /// @param y        The y coordinate
    /// @param width    The width of the area to draw
    /// @param height   The height of the area to draw
    /// @param useBrush Whether to use the currently set brush to apply to the texture
    private void drawSpriteNineSliced(final TextureAtlasSprite sprite, final GuiSpriteScaling.NineSlice scaling,
                                      final int x, final int y, final int width, final int height,
                                      final boolean useBrush)
    {
        this.drawSpriteNineSliced(sprite, scaling, x, y, width, height, UvMapping.FULL, useBrush);
    }
    
    /// Draws a sprite on the screen that is 9-sliced
    /// (see <a href="https://en.wikipedia.org/wiki/9-slice_scaling">9-slice scaling</a>).
    /// @param sprite   The sprite to draw (can be fetched from the texture atlas)
    /// @param scaling  The [GuiSpriteScaling.NineSlice] scaling
    /// @param rect     The [Rectangle] to draw the texture in
    /// @param useBrush Whether to use the currently set brush to apply to the texture
    private void drawSpriteNineSliced(final TextureAtlasSprite sprite, final GuiSpriteScaling.NineSlice scaling,
                                      final Rectangle rect, final boolean useBrush)
    {
        rect.accept((x, y, w, h) -> this.drawSpriteNineSliced(sprite, scaling, x, y, w, h, useBrush));
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private void drawSpriteRegion(final TextureAtlasSprite sprite,
                                  final int x, final int y, final int width, final int height,
                                  final int u, final int v, final int textureWidth, final int textureHeight,
                                  final boolean useBrush)
    {
        if (width != 0 && height != 0)
        {
            this.drawTexturedQuadSprite(sprite, x, y, (x + width), (y + height), (u / (float) textureWidth),
                                        (v / (float) textureHeight), ((u + width) / (float) textureWidth),
                                        ((v + height) / (float) textureHeight), useBrush);
        }
    }
    
    private void drawSpriteTiled(final TextureAtlasSprite sprite,
                                 final int x, final int y, final int width, final int height,
                                 final int u, final int v, final int textureWidth, final int textureHeight,
                                 final int tileWidth, final int tileHeight,
                                 final boolean useBrush)
    {
        if (width <= 0 || height <= 0)
        {
            return;
        }

        if (tileWidth <= 0 || tileHeight <= 0)
        {
            throw new IllegalArgumentException(
                "Tiled sprite texture size must be positive, got %sx%s".formatted(tileWidth, tileHeight));
        }

        for (int ix = 0; ix < width; ix += tileWidth)
        {
            final int draw_width = Math.min(tileWidth, (width - ix));
            final int draw_x     = (x + ix);

            for (int iy = 0; iy < height; iy += tileHeight)
            {
                final int draw_height = Math.min(tileHeight, (height - iy));
                final int draw_y      = (y + iy);

                this.drawSpriteRegion(sprite, draw_x, draw_y, draw_width, draw_height,
                                      u, v, textureWidth, textureHeight,
                                      useBrush);
            }
        }
    }

    private void drawInnerSprite(final GuiSpriteScaling.NineSlice nineSlice, final TextureAtlasSprite sprite,
                                 final int x, final int y, final int width, final int height,
                                 final int u, final int v, final int textureWidth, final int textureHeight,
                                 final int tileWidth, final int tileHeight, final boolean useBrush)
    {
        if (width <= 0 || height <= 0)
        {
            return;
        }

        if (nineSlice.stretchInner())
        {
            this.drawTexturedQuadSprite(sprite, x, y, (x + width), (y + height), (u / (float) textureWidth),
                                        (v / (float) textureHeight), ((u + tileWidth) / (float) textureWidth),
                                        ((v + tileHeight) / (float) textureHeight), useBrush);
        }
        else
        {
            this.drawSpriteTiled(sprite, x, y, width, height, u, v, textureWidth, textureHeight, tileWidth, tileHeight,
                                 useBrush);
        }
    }
    
    private void drawTexturedQuadSprite(final TextureAtlasSprite sprite,
                                        final int x1, final int y1, final int x2, final int y2,
                                        final float u1, final float v1, final float u2, final float v2,
                                        final boolean useBrush)
    {
        this.drawTexturedQuad(
            sprite.atlasLocation(),
            x1, y1, x2, y2,
            sprite.getU(u1), sprite.getV(v1), sprite.getU(u2), sprite.getV(v2),
            useBrush);
    }
    
    //==================================================================================================================
    /// Draws a region of a texture on the screen as-is and stretched to the target area.
    /// @param textureId The id of the texture inside the assets' root folder
    /// @param x         The left coordinate
    /// @param y         The y coordinate
    /// @param width     The width of the area to draw
    /// @param height    The height of the area to draw
    /// @param uv        The [UvMapping] texture region to use as 9-slice, this is the part that will be repeated
    /// @param useBrush  Whether to use the currently set brush to apply to the texture
    public void drawTexture(final Identifier textureId,
                            final int x, final int y, final int width, final int height,
                            final UvMapping uv, final boolean useBrush)
    {
        this.drawTexturedQuad(textureId, x, y, (x + width), (y + height), uv.minU(), uv.minV(), uv.maxU(), uv.maxV(),
                              useBrush);
    }

    /// Draws a region of a texture on the screen as-is and stretched to the target area.
    /// @param textureId The id of the texture inside the assets' root folder
    /// @param rect      The [Rectangle] to draw the texture in
    /// @param uv        The [UvMapping] texture region to use as 9-slice, this is the part that will be repeated
    /// @param useBrush  Whether to use the currently set brush to apply to the texture
    public void drawTexture(final Identifier textureId, final Rectangle rect, final UvMapping uv,
                            final boolean useBrush)
    {
        rect.accept((x, y, w, h) -> this.drawTexture(textureId, x, y, w, h, uv, useBrush));
    }

    /// Draws a texture on the screen as-is and stretched to the target area.
    /// @param textureId The id of the texture inside the assets' root folder
    /// @param x         The left coordinate
    /// @param y         The y coordinate
    /// @param width     The width of the area to draw
    /// @param height    The height of the area to draw
    /// @param useBrush  Whether to use the currently set brush to apply to the texture
    public void drawTexture(final Identifier textureId,
                            final int x, final int y, final int width, final int height, final boolean useBrush)
    {
        this.drawTexture(textureId, x, y, width, height, UvMapping.FULL, useBrush);
    }

    /// Draws a texture on the screen as-is and stretched to the target area.
    /// @param textureId The id of the texture inside the assets' root folder
    /// @param rect      The [Rectangle] to draw the texture in
    /// @param useBrush  Whether to use the currently set brush to apply to the texture
    public void drawTexture(final Identifier textureId, final Rectangle rect, final boolean useBrush)
    {
        rect.accept((x, y, w, h) -> this.drawTexture(textureId, x, y, w, h, useBrush));
    }

    //------------------------------------------------------------------------------------------------------------------
    private void drawTexturedQuad(final Identifier textureId,
                                  final int x1, final int y1, final int x2, final int y2,
                                  final float u1, final float v1, final float u2, final float v2,
                                  final boolean useBrush)
    {
        final AbstractTexture texture = this.client.getTextureManager().getTexture(textureId);
        this.drawTexturedQuad(texture, x1, y1, x2, y2, u1, v1, u2, v2, useBrush);
    }

    private void drawTexturedQuad(final AbstractTexture texture,
                                  final int x1, final int y1, final int x2, final int y2,
                                  final float u1, final float v1, final float u2, final float v2,
                                  final boolean useBrush)
    {
        final Rectangle bounds = Rectangle.fromPoints(x1, y1, x2, y2);
        
        if (!useBrush)
        {
            final int colour = Colour.WHITE.withOpacityRel(this.getOpacity()).colour();
            this.draw(new ICanvasState.Renderable(
                ((vertices, matrix) -> DrawUtil.drawTexturedRect(
                    matrix, vertices,
                    x1, y1, x2, y2,
                    u1, v1, u2, v2,
                    colour, colour, colour, colour)),
                TextureSetup.singleTexture(texture.getTextureView(), texture.getSampler()),
                null,
                bounds
            ));
            
            return;
        }

        this.drawShaped(new ShapedRectangleRender.Texture(x1, y1, x2, y2, u1, v1, u2, v2), bounds, texture);
    }

    //==================================================================================================================
    /// Creates a new temporary state for the current canvas frame that remembers the state prior to this call and
    /// reverts to it upon calling [#popState()].
    ///
    /// This is useful when needing to do transforms and clipping regions or when a brush needs to be remembered for
    /// later.
    ///
    /// There is also [#runWithState(Runnable)] which hides pushing and popping behind a callback so that this
    /// does not have to be manually managed.
    public void pushState()
    {
        this.statebuffer.addLast(Objects.requireNonNull(this.state));
        this.state = new State(this.state);
    }

    /// Pops the current frame's state and reverts to the state prior to [#pushState()].
    ///
    /// Do note that this does not need to be called if you don't need to revert to a previous state, however, if this
    /// is the case, a temporary state would not be needed to begin with.
    /// @throws NoSuchElementException If no [#pushState()] has happened prior to this call
    public void popState() { this.state = this.statebuffer.removeLast(); }

    /// Pops all pushed states and initialises the default state of the current frame. This can be used to revert the
    /// frame state to the default.
    public void resetState()
    {
        this.statebuffer.clear();
        this.state = Objects.requireNonNull(this.frame).createState();
    }

    /// Automatically pushes and pops a new state context and executes the given action between the push and pop.
    ///
    /// This means, any brush, matrix and clipping updates applied during the given action are reverted to before the
    /// action was invoked after it finished.
    ///
    /// This should be preferred whenever possible as it takes away the responsibility for managing a state switch,
    /// hence no manual intervention is needed.
    ///
    /// One drawback of this is that sometimes IDEs might not be able to hotswap if the runnable changes and
    /// hence a complete reload becomes necessary.
    /// @param action The action to run during the current temporary state
    public void runWithState(final Runnable action)
    {
        this.pushState();
        action.run();
        this.popState();
    }

    //==================================================================================================================
    void pushFrame(final GuiComponent component)
    {
        Objects.requireNonNull(component, "component must not be null");
        this.framebuffer.addLast(Objects.requireNonNull(this.frame));
        this.frame = Frame.forComponent(component, this.frame);
        this.resetState();
    }

    void popFrame() { this.frame = this.framebuffer.removeLast(); }

    void setLayer(final ScreenLayer layer)
    {
        this.framebuffer.clear();
        this.frame = Frame.forLayer(layer);
        this.resetState();
        this.renderState.nextStratum();
    }

    void initFramebuffer(final GuiScreen screen)
    {
        this.frame = new Frame(screen.getScreenBounds(), screen.getFont(), screen.getTemplate(), screen,
                               new AffineTransform(), 1.0f, true);
        this.state = this.frame.createState();
    }
}
