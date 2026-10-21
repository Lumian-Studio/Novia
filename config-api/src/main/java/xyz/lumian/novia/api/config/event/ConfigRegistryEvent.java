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
package xyz.lumian.novia.api.config.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.resources.Identifier;
import xyz.lumian.novia.api.config.registry.ConfigRegistry;
import xyz.lumian.novia.api.config.provider.IConfigProvider;

import java.util.*;



//**********************************************************************************************************************
public abstract class ConfigRegistryEvent
{
    //******************************************************************************************************************
    public static final Event<RegistryFreezingCallback> FREEZING = EventFactory
        .createArrayBacked(
            RegistryFreezingCallback.class,
            (listeners -> (registry -> Arrays
                .stream(listeners)
                .forEach(listener -> listener.handle(registry)))));
    
    public static final Event<RegistryRegistrationCallback> REGISTER = EventFactory
        .createArrayBacked(
            RegistryRegistrationCallback.class,
            (listeners -> ((registry, id, provider) -> Arrays
                .stream(listeners)
                .forEach(listener -> listener.handle(registry, id, provider)))));
    
    //******************************************************************************************************************
    @FunctionalInterface
    public interface RegistryFreezingCallback
    {
        //**************************************************************************************************************
        void handle(final ConfigRegistry registry);
    }
    
    @FunctionalInterface
    public interface RegistryRegistrationCallback
    {
        //**************************************************************************************************************
        void handle(final ConfigRegistry registry, final Identifier providerId, final IConfigProvider<?> provider);
    }
}
