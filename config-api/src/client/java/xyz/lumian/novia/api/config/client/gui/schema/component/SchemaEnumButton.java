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
package xyz.lumian.novia.api.config.client.gui.schema.component;

import com.mojang.datafixers.util.Pair;
import net.minecraft.network.chat.Component;
import xyz.lumian.novia.api.core.serialisation.Value;
import xyz.lumian.novia.api.gui.event.GuiEvent;

import java.util.*;
import java.util.stream.IntStream;



//**********************************************************************************************************************
public class SchemaEnumButton
    extends SchemaButton
{
    //******************************************************************************************************************
    public GuiEvent.Simple valueChanged = new GuiEvent.Simple();
    
    //==================================================================================================================
    private final List<Pair<Component, Value>> values;
    
    private int currentIndex = 0;
    
    //******************************************************************************************************************
    public SchemaEnumButton(final List<Pair<Component, Value>> values)
    {
        Objects.requireNonNull(values, "value list must not be null");
        
        if (values.isEmpty())
        {
            throw new IllegalArgumentException("value list must provide at least one option");
        }
        
        this.values = values;
        this.updateMessage();
        
        this.clicked.subscribe((sender, e) -> this.updateState());
    }
    
    //==================================================================================================================
    @Override public Value           getValue()       { return this.values.get(this.currentIndex).getSecond(); }
    @Override public GuiEvent.Simple getChangeEvent() { return this.valueChanged; }
    
    //==================================================================================================================
    @Override
    public void setValue(final Value value)
    {
        this.setIndex(IntStream
            .range(0, this.values.size())
            .filter(i -> (this.values.get(i).getSecond().equals(value)))
            .findAny()
            .orElse(this.currentIndex));
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private void setIndex(final int index)
    {
        if (index == this.currentIndex)
        {
            return;
        }
        
        this.currentIndex = index;
        
        this.updateMessage();
        this.notifyChangeListeners();
    }
    
    //==================================================================================================================
    private void updateMessage() { this.setText(this.values.get(this.currentIndex).getFirst()); }
    private void updateState()   { this.setIndex((this.currentIndex + 1) % this.values.size()); }
}
