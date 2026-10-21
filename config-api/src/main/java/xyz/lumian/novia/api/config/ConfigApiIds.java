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


import net.minecraft.resources.Identifier;



//**********************************************************************************************************************
public class ConfigApiIds
{
    //******************************************************************************************************************
    public static abstract class Payload
    {
        //**************************************************************************************************************
        // Client-bound : Config
        public static final Identifier CONFIG_S2C_CONFIG_SYNC = of("cs2c_config_sync");
        
        //==============================================================================================================
        // Client-bound : Play
        public static final Identifier PLAY_S2C_CONFIG_SINGLE = of("ps2c_config_single");
        public static final Identifier PLAY_S2C_CONFIG_BULK   = of("ps2c_config_bulk");
        public static final Identifier PLAY_S2C_CONFIG_SYNC   = of("ps2c_config_sync");
        
        //==============================================================================================================
        // Server-bound : Config
        public static final Identifier CONFIG_C2S_CONFIG_ACK = of("cc2s_config_ack");
        
        //==============================================================================================================
        // Server-bound : Play
        public static final Identifier CONFIG_C2S_SERIALISE = of("pc2s_serialise");
    }
    
    public static abstract class Config
    {
        //**************************************************************************************************************
        public static final Identifier CATEGORY_GENERAL = of("general");
    }
    
    public static abstract class Registry
    {
        //**************************************************************************************************************
        public static final Identifier CONFIG_OVERRIDE = Identifier.fromNamespaceAndPath("novia", "config");
    }
    
    public static abstract class ResourceListener
    {
        //**************************************************************************************************************
        public static final Identifier CONFIG_SCREEN = of("config_screen");
    }
    
    public static abstract class Sprite
    {
        //**************************************************************************************************************
        public static final Identifier CONFIG_BACKGROUND_TABS    = of("config/background_tabs");
        public static final Identifier CONFIG_BACKGROUND_OPTIONS = of("config/background_options");
        public static final Identifier CONFIG_BACKGROUND_EXTRA   = of("config/background_extra");
        
        public static final Identifier CONFIG_TAB_BUTTON              = of("widget/tab");
        public static final Identifier CONFIG_TAB_BUTTON_INTERMEDIATE = of("widget/tab_intermediate");
        public static final Identifier CONFIG_TAB_BUTTON_SELECTED     = of("widget/tab_selected");
    }
    
    //******************************************************************************************************************
    public static Identifier of(final String path) { return Identifier.fromNamespaceAndPath(ConfigDefine.ID, path); }
}
