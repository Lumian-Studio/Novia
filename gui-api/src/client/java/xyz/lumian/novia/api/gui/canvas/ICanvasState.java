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

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.render.TextureSetup;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import xyz.lumian.novia.api.gui.geometry.Rectangle;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;

import java.util.Objects;



//**********************************************************************************************************************
/// Provides an abstraction layer on top of Minecraft's [GuiElementRenderState] that abstracts away some gory
/// implementation details, which should make it a little easier dealing with render states.
public interface ICanvasState
{
    //******************************************************************************************************************
    /// The rendering function to be used to set up the vertices of the render state.
    @FunctionalInterface
    interface Renderer
    {
        //**************************************************************************************************************
        /// Sets up the vertices to render.
        /// @param vertices The [VertexConsumer]
        /// @param matrix   The [Matrix3x2f] transformation matrix
        void render(final VertexConsumer vertices, final Matrix3x2f matrix);
        
        /// Attaches a [Renderer] that should render before this one.
        /// @param before The [Renderer] to render before this one
        /// @return The new [Renderer] context
        default Renderer preRender(final Renderer before)
        {
            return ((vertices, matrix) ->
            {
                before.render(vertices, matrix);
                this.render(vertices, matrix);
            });
        }
        
        /// Attaches a [Renderer] that should render after this one.
        /// @param after The [Renderer] to render after this one
        /// @return The new [Renderer] context
        default Renderer postRender(final Renderer after)
        {
            return ((vertices, matrix) ->
            {
                this.render(vertices, matrix);
                after.render(vertices, matrix);
            });
        }
    }
    
    /// Provides a simple and quick implementation of the [ICanvasState] interface.
    /// @param renderer The [Renderer] that sets up the vertices
    /// @param texture  The [TextureSetup] to bind to the render state, or `null` to not use any textures
    /// @param pipeline The [RenderPipeline] to use, or `null` to automatically determine the pipeline
    ///                 (see [ICanvasState#pipeline()])
    /// @param bounds   The bounds [Rectangle] to draw the contents in, or `null` to use the current frame's rectangle
    record Renderable(
                  Renderer       renderer,
        @Nullable TextureSetup   texture,
        @Nullable RenderPipeline pipeline,
        @Nullable Rectangle      bounds
    ) implements ICanvasState {}
    
    /// Provides an abstract [ICanvasState] that can be overridden to provide the renderer instead of passing it as an
    /// argument.
    abstract class Drawable
        implements
            ICanvasState,
            Renderer
    {
        //**************************************************************************************************************
        private final @Nullable TextureSetup   texture;
        private final @Nullable RenderPipeline pipeline;
        private final @Nullable Rectangle      bounds;
        
        //**************************************************************************************************************
        /// Constructs a new drawable render state.
        /// @param texture  The [TextureSetup] to bind to the render state, or `null` to not use any textures
        /// @param pipeline The [RenderPipeline] to use, or `null` to automatically determine the pipeline
        ///                 (see [ICanvasState#pipeline()])
        /// @param bounds   The bounds [Rectangle] to draw the contents in, or `null` to use the current frame's
        ///                 rectangle
        public Drawable(final @Nullable TextureSetup   texture,
                        final @Nullable RenderPipeline pipeline,
                        final @Nullable Rectangle      bounds)
        {
            this.texture  = texture;
            this.pipeline = pipeline;
            this.bounds   = bounds;
        }
        
        //===================================s===========================================================================
        @Override public           Renderer       renderer() { return this; }
        @Override public @Nullable TextureSetup   texture()  { return this.texture; }
        @Override public @Nullable RenderPipeline pipeline() { return this.pipeline; }
        @Override public @Nullable Rectangle      bounds()   { return this.bounds; }
    }
    
    /// Provides a render state that allows setting the renderer at a later stage with
    /// [Deferred#bindRenderer(Renderer)].
    ///
    /// If the [Canvas] ends up rendering this state without a late-bound renderer,
    /// this will throw a [NullPointerException].
    class Deferred
        implements ICanvasState
    {
        //**************************************************************************************************************
        private final @Nullable TextureSetup   texture;
        private final @Nullable RenderPipeline pipeline;
        private final @Nullable Rectangle      bounds;
        
        private @Nullable Renderer renderer;
        
        //**************************************************************************************************************
        /// Constructs a new deferred render state with the given initial renderer.
        /// @param renderer The [Renderer] that sets up the vertices, set to `null` to bind later
        /// @param texture  The [TextureSetup] to bind to the render state, or `null` to not use any textures
        /// @param pipeline The [RenderPipeline] to use, or `null` to automatically determine the pipeline
        ///                 (see [ICanvasState#pipeline()])
        /// @param bounds   The bounds [Rectangle] to draw the contents in, or `null` to use the current frame's
        ///                 rectangle
        public Deferred(final @Nullable Renderer       renderer,
                        final @Nullable TextureSetup   texture,
                        final @Nullable RenderPipeline pipeline,
                        final @Nullable Rectangle      bounds)
        {
            this.renderer = renderer;
            this.texture  = texture;
            this.pipeline = pipeline;
            this.bounds   = bounds;
        }
        
        /// Constructs a new deferred render state.
        /// @param texture  The [TextureSetup] to bind to the render state, or `null` to not use any textures
        /// @param pipeline The [RenderPipeline] to use, or `null` to automatically determine the pipeline
        ///                 (see [ICanvasState#pipeline()])
        /// @param bounds   The bounds [Rectangle] to draw the contents in, or `null` to use the current frame's
        ///                 rectangle
        public Deferred(final @Nullable TextureSetup   texture,
                        final @Nullable RenderPipeline pipeline,
                        final @Nullable Rectangle      bounds)
        {
            this(null, texture, pipeline, bounds);
        }
        
        //==============================================================================================================
        /// Binds the [Renderer] to be used to this render state
        /// @param renderer The renderer to bind
        public void bindRenderer(final Renderer renderer) { this.renderer = Objects.requireNonNull(renderer); }
        
        //==============================================================================================================
        @Override public           Renderer       renderer() { return Objects.requireNonNull(this.renderer); }
        @Override public @Nullable TextureSetup   texture()  { return this.texture; }
        @Override public @Nullable RenderPipeline pipeline() { return this.pipeline; }
        @Override public @Nullable Rectangle      bounds()   { return this.bounds; }
    }
    
    //******************************************************************************************************************
    /// Describes the [Renderer] to be used to draw this state.
    /// @return The [Renderer]
    Renderer renderer();
    
    /// Describes the [TextureSetup] to be used to draw onto this render state. Setting this to `null` has the same
    /// effect as supplying [TextureSetup#noTexture()],
    ///
    /// This might affect the used [RenderPipeline], see [ICanvasState#pipeline()] for more details.
    /// @return The [TextureSetup]
    @Nullable TextureSetup texture();
    
    /// Describes the [RenderPipeline] to be used to draw this state with.
    ///
    /// If `null`, this will automatically become [Canvas#NOVIA_GUI], unless [ICanvasState#texture()] is non-null and
    /// has at least [TextureSetup#texure0()] or [TextureSetup#texure1()] set to a non-null value, in which case this
    /// will default to [Canvas#NOVIA_GUI_TEXTURED].
    /// @return The [RenderPipeline]
    @Nullable RenderPipeline pipeline();
    
    /// Describes the bounds of the object to be drawn on screen. If `null`, the canvas' current frame rectangle is
    /// used instead.
    ///
    /// Do note that the resulting rectangle is influenced by the current clipping region as well as the given
    /// [AffineTransform], by that means, the final area to be drawn will be the transformed version which can not
    /// exceed the clipping region.
    /// @return The bounds [Rectangle]
    @Nullable Rectangle bounds();
}
