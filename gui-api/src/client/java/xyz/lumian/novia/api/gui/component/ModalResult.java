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
package xyz.lumian.novia.api.gui.component;

import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;



//**********************************************************************************************************************
/// A result for a modal component operation (with [GuiScreen#showModal(GuiComponent, ModalArgs)] or
/// [GuiComponent#showModal(ModalArgs)]), containing either an error [Code] or, if successfully opened, represents a
/// completed future, completed when the modal was closed again.
///
/// The future represented by this class is completed on the main render thread of Minecraft, hence it would be
/// advisable not to block on that thread.
public class ModalResult
    extends CompletableFuture<@Nullable GuiComponent>
{
    //******************************************************************************************************************
    public enum Code
    {
        /// When the modal is already open on screen.
        ALREADY_SHOWING,
        
        /// When trying to add a modal to a non-opened screen.
        SCREEN_NOT_OPEN,
        
        /// When trying to open an unsupported [GuiComponent] that overrides [GuiComponent#isModalPromotionAllowed()]
        ///returning `false`.
        /// @see GuiScreen#isModalPromotionAllowed()
        NOT_SUPPORTED,
        
        /// The given associated component is either not on screen or invisible.
        ASSOCIATED_MISSING,
        ;
    }
    
    //******************************************************************************************************************
    @Nullable
    private final Code code;
    
    //******************************************************************************************************************
    public ModalResult(final Code code)
    {
        this.code = Objects.requireNonNull(code, "code must not be null");
        this.complete(null);
    }
    
    public ModalResult() { this.code = null; }
    
    //==================================================================================================================
    /// Gets the result code associated with the layer opening operation.
    ///
    /// If this returns a non-empty optional, this [CompletableFuture] will never be completed as the operation
    /// failed, which makes an early bailing out possible if desired.
    /// @return The result code or [Optional#empty()] if there was no issue opening the layer
    public Optional<Code> getCode() { return Optional.ofNullable(this.code); }
    
    //==================================================================================================================
    /// If the modal was opened successfully, immediately runs the given `handler`.
    /// @param handler The handler to run
    /// @return `this`
    public ModalResult onOpened(final Runnable handler)
    {
        if (this.code == null)
        {
            handler.run();
        }
        
        return this;
    }
    
    /// If the modal could not be opened, immediately runs the given `consumer` with the error code.
    /// @param consumer The consumer to run on error
    /// @return `this`
    public ModalResult onError(final Consumer<Code> consumer)
    {
        Optional.ofNullable(this.code).ifPresent(consumer);
        return this;
    }
}
