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
package xyz.lumian.novia.api.gui.geometry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;



//**********************************************************************************************************************
public record UvMapping(float minU, float minV, float maxU, float maxV)
{
    //******************************************************************************************************************
    public static final Codec<UvMapping> CODEC = RecordCodecBuilder.create(instance -> instance
        .group(
            Codec.FLOAT.fieldOf("minU").forGetter(UvMapping::minU),
            Codec.FLOAT.fieldOf("minV").forGetter(UvMapping::minV),
            Codec.FLOAT.fieldOf("maxU").forGetter(UvMapping::maxU),
            Codec.FLOAT.fieldOf("maxV").forGetter(UvMapping::maxV))
        .apply(instance, UvMapping::new));
    
    /** Describes the entire texture. */
    public static final UvMapping FULL = new UvMapping(0f, 0f, 1f, 1f);
    
    //******************************************************************************************************************
    public static UvMapping mapped(final int u, final int v, final int textureWidth, final int textureHeight)
    {
        return new UvMapping((u / (float) textureWidth), (v / (float) textureHeight), 1f, 1f);
    }
    
    public static UvMapping mapped(final Rectangle uvRect) { return uvRect.transform(UvMapping::mapped); }
    
    public static UvMapping region(final int u, final int v,
                                   final int textureWidth, final int textureHeight,
                                   final int regionWidth, final int regionHeight)
    {
        return new UvMapping(
            (u / (float) textureWidth),
            (v / (float) textureHeight),
            ((u + regionWidth) / (float) textureWidth),
            ((v + regionHeight) / (float) textureHeight));
    }
    
    public static UvMapping region(final Rectangle uvRect, final int regionWidth, final int regionHeight)
    {
        return uvRect.transform((u, v, w, h) -> UvMapping.region(u, v, w, h, regionWidth, regionHeight));
    }
    
    public static UvMapping fromTexture(final SpriteId spriteId, final int u, final int v)
    {
        final SpriteContents contents = Minecraft.getInstance().getAtlasManager().get(spriteId).contents();
        return UvMapping.mapped(u, v, contents.width(), contents.height());
    }
    
    public static UvMapping fromTexture(final Identifier spriteId, final int u, final int v)
    {
        final SpriteContents contents = Minecraft.getInstance().getAtlasManager()
            .getAtlasOrThrow(AtlasIds.GUI)
            .getSprite(spriteId)
            .contents();
        return UvMapping.mapped(u, v, contents.width(), contents.height());
    }
    
    public static UvMapping fromTextureRegion(final SpriteId spriteId,
                                              final int u, final int v, final int regionWidth, final int regionHeight)
    {
        final SpriteContents contents = Minecraft.getInstance().getAtlasManager().get(spriteId).contents();
        return UvMapping.region(u, v, contents.width(), contents.height(), regionWidth, regionHeight);
    }
    
    public static UvMapping fromTextureRegion(final Identifier spriteId,
                                              final int u, final int v, final int regionWidth, final int regionHeight)
    {
        final SpriteContents contents = Minecraft.getInstance().getAtlasManager()
            .getAtlasOrThrow(AtlasIds.GUI)
            .getSprite(spriteId)
            .contents();
        return UvMapping.region(u, v, contents.width(), contents.height(), regionWidth, regionHeight);
    }
    
    //******************************************************************************************************************
    public UvMapping
    {
        if (minU < 0f || minV < 0f)
        {
            throw new IllegalArgumentException("minU and minV must be positive");
        }
        
        if (maxU > 1f || maxV > 1f)
        {
            throw new IllegalArgumentException("maxU and maxV must be less than or equal to 1");
        }
        
        if (minU > maxU || minV > maxV)
        {
            throw new IllegalArgumentException("minU and minV must be less or equal to maxU and maxV");
        }
    }
    
    public UvMapping(float u, float v) { this(u, v, 1f, 1f); }
}
