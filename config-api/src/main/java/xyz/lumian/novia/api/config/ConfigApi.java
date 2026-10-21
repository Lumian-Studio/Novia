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
package xyz.lumian.novia.api.config;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import xyz.lumian.novia.api.config.network.payload.ConfigApiPayloads;
import xyz.lumian.novia.api.config.spec.ConfigSpec;
import xyz.lumian.novia.api.core.util.FragmentId;



//**********************************************************************************************************************
public class ConfigApi
    implements ModInitializer
{
    //******************************************************************************************************************
    public static final Identifier SCHEMA_STD        = ConfigApiIds.of("std");
    public static final FragmentId ID_SCHEMA         = new FragmentId(SCHEMA_STD, "identifier");
    public static final FragmentId TAG_SCHEMA        = new FragmentId(SCHEMA_STD, "tag");
    public static final FragmentId TAG_OR_ID_SCHEMA  = new FragmentId(SCHEMA_STD, "tag_or_identifier");
    public static final FragmentId COLOUR_SCHEMA     = new FragmentId(SCHEMA_STD, "colour");
    public static final FragmentId HEX_COLOUR_SCHEMA = new FragmentId(SCHEMA_STD, "hex_colour");
    public static final FragmentId UUID_SCHEMA       = new FragmentId(SCHEMA_STD, "uuid");
    
    //==================================================================================================================
    static
    {
        ConfigSpec.SCHEMA_STORE.loadSchemas(ConfigDefine.ID);
    }
    
    //******************************************************************************************************************
    @Override public void onInitialize() { ConfigApiPayloads.initialise(); }
}
