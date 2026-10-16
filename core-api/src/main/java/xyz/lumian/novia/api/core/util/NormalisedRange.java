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
package xyz.lumian.novia.api.core.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.InclusiveRange;



//**********************************************************************************************************************
/// Represents a number range that can be converted from and to the normalised/denormalised domain.
///
/// A denormalised number is any number that is representable by the Java double type, while a normalised value
/// represents a given range's min/max constraints as a value between (and including) 0 and 1, where 0 is the range
/// minimum, 1 is the maximum and anything in-between is a decimal value that is the normalised representation of a
/// given denormalised value.
///
/// Furthermore, this class provides two additional features for fine-tuning the conversion process between these two
/// domains, on one hand there is the step size which, when using the [#snap(double)] function, makes sure that
/// the given value is snapped to a discrete number in bounds of the given range constraints and the step size. The
/// second feature is the conversion skew, which allows skewing the converted value (in both domains) logarithmically.
public record NormalisedRange(double min, double max, double step, double skew)
{
    //******************************************************************************************************************
    /// Represents the full `double` range (-[Double#MAX_VALUE] to [Double#MAX_VALUE]).
    public static final NormalisedRange FULL_RANGE = new NormalisedRange(-Double.MAX_VALUE, Double.MAX_VALUE);
    
    /// Represents the full positive `double` range (0 to [Double#MAX_VALUE]).
    public static final NormalisedRange POSITIVE_RANGE = new NormalisedRange(0, Double.MAX_VALUE);
    
    /// Represents the full negative `double` range (-[Double#MAX_VALUE] to -0).
    public static final NormalisedRange NEGATIVE_RANGE = new NormalisedRange(-Double.MAX_VALUE, 0);
    
    /// The packet coded for this class.
    public static final StreamCodec<FriendlyByteBuf, NormalisedRange> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.DOUBLE, NormalisedRange::min,
        ByteBufCodecs.DOUBLE, NormalisedRange::max,
        ByteBufCodecs.DOUBLE, NormalisedRange::step,
        ByteBufCodecs.DOUBLE, NormalisedRange::skew,
        NormalisedRange::new);
    
    /// The serialisation coded for this class.
    public static final Codec<NormalisedRange> CODEC = RecordCodecBuilder.create(instance -> instance
        .group(
            Codec.DOUBLE
                .optionalFieldOf("min", -Double.MAX_VALUE)
                .forGetter(NormalisedRange::min),
            Codec.DOUBLE
                .optionalFieldOf("max", Double.MAX_VALUE)
                .forGetter(NormalisedRange::max),
            Codec.DOUBLE
                .optionalFieldOf("step", 1.0)
                .forGetter(NormalisedRange::step),
            Codec.DOUBLE
                .optionalFieldOf("skew", 1.0)
                .forGetter(NormalisedRange::skew))
        .apply(instance, NormalisedRange::new));
    
    //******************************************************************************************************************
    /// Constructs a new normalised range from the given range.
    /// @param min  The minimum value of the range
    /// @param max  The maximum value of the range
    /// @param step The stepping size (only important when using [NormalisedRange#snap(double)])
    /// @param skew The skew factor (pow and log) of the conversion between the different range domains
    /// @throws IllegalArgumentException If skew is less than or equal to 0, step is less than 0, or if min is greater
    ///                                  than max
    public NormalisedRange
    {
        if (min > max)
        {
            throw new IllegalArgumentException("Minimum %s was bigger than maximum %s".formatted(min, max));
        }
        
        if (step < 0)
        {
            throw new IllegalArgumentException("Step cannot be negative");
        }
        
        if (skew <= 0)
        {
            throw new IllegalArgumentException("Skew cannot be negative and must be greater than zero");
        }
    }
    
    /// Constructs a new normalised range from the given range, with a step size of 0 and a skew of 1.
    /// @param range The [InclusiveRange] to gather min and max from
    public NormalisedRange(final InclusiveRange<Double> range) { this(range, 0.0, 1.0); }
    
    /// Constructs a new normalised range from the given min/max, with a step size of 0 and a skew of 1.
    /// @param min The minimum value of the range
    /// @param max The maximum value of the range
    /// @throws IllegalArgumentException If min is greater than max
    public NormalisedRange(final double min, final double max) { this(min, max, 0.0, 1.0); }
    
    /// Constructs a new normalised range from the given range, with a skew of 1.
    /// @param range The [InclusiveRange] to gather min and max from
    /// @param step  The stepping size (only important when using [NormalisedRange#snap(double)])
    /// @throws IllegalArgumentException If step is less than 0
    public NormalisedRange(final InclusiveRange<Double> range, final double step) { this(range, step, 1.0); }
    
    /// Constructs a new normalised range from the given range, with a skew of 1.
    /// @param min  The minimum value of the range
    /// @param max  The maximum value of the range
    /// @param step The stepping size (only important when using [NormalisedRange#snap(double)])
    /// @throws IllegalArgumentException If step is less than 0, or if min is greater than max
    public NormalisedRange(final double min, final double max, final double step) { this(min, max, step, 1.0); }
    
    /// Constructs a new normalised range from the given range.
    /// @param range The [InclusiveRange] to gather min and max from
    /// @param step  The stepping size (only important when using [NormalisedRange#snap(double)])
    /// @param skew  The skew factor (pow and log) of the conversion between the different range domains
    /// @throws IllegalArgumentException If skew is less than or equal to 0, or if step is less than 0
    public NormalisedRange(final InclusiveRange<Double> range, final double step, final double skew)
    {
        this(range.minInclusive(), range.maxInclusive(), step, skew);
    }
    
    //==================================================================================================================
    /// Gets the distance between the minimum and the maximum of this range.
    /// @return The length of the range
    public double getDistance() { return (this.max - this.min); }
    
    /// Converts this range to a Minecraft range object.
    /// @return The new [InclusiveRange]
    public InclusiveRange<Double> getRange() { return new InclusiveRange<>(this.min, this.max); }
    
    //==================================================================================================================
    /// Determines whether the given number is inside the min/max constraints of this range.
    /// @param value The number to check
    /// @return `true` if the given value is in range, otherwise `false`
    public boolean isInRange(final Number value)
    {
        final double val = value.doubleValue();
        return (val >= this.min && val <= this.max);
    }
    
    //==================================================================================================================
    /// Normalises the given value relative to the min/max constraints of this range.
    ///
    /// If this range provides a skew value that is not 1, the given value will be normalised but skewed logarithmically
    /// using the [Math#pow(double, double)] function. For example, consider we have the range min=0 and max=10,
    /// usually when normalising a value like 5, the result should be 0.5, however, if we are providing a skew of 2,
    /// the resulting normalised value will be "0.25"
    ///
    /// Check out this [Desmos graph](https://www.desmos.com/calculator/rtncdj0wd1) for testing different inputs.
    ///
    /// @param value The value to normalise
    /// @return The normalised value
    public float normalise(final double value)
    {
        final float normalised = Math.clamp((float) ((value - this.min) / this.getDistance()), 0f, 1f);
        
        if (this.skew == 1.0)
        {
            return normalised;
        }
        
        return (float) Math.pow(normalised, this.skew);
    }
    
    /// Denormalises the given value relative to the min/max constraints of this range.
    ///
    /// If this range provides a skew value that is not 1, the given value will be de normalised but skewed
    /// logarithmically using the [Math#log(double)] function. For example, consider we have the range
    /// min=0 and max=10, usually when denormalising a value like 0.5, the result should be 5, however,
    /// if we are providing a skew of 2, the resulting denormalised value will be "7.071...".
    ///
    /// Check out this [Desmos graph](https://www.desmos.com/calculator/2rgzdsgabl) for testing different inputs.
    ///
    /// @param normalised The value to denormalise
    /// @return The denormalised value
    public double denormalise(final float normalised)
    {
        double adjusted = Math.clamp(normalised, 0f, 1f);
        
        if (this.skew != 1.0 && adjusted > 0.0)
        {
            adjusted = (float) Math.exp(Math.log(adjusted) / this.skew);
        }
        
        return (this.min + (this.getDistance() * adjusted));
    }
    
    /// Clamps the denormalised value to the given min/max constraints of this range.
    /// @param value The denormalised value to clamp
    /// @return The clamped denormalised value
    public double clamp(final double value) { return Math.clamp(value, this.min, this.max); }
    
    /// Snaps the denormalised value to the given min/max constraints of this range and step value.
    ///
    /// If this range provides a snap value that is not 0, the given value will be snapped to the closest interval
    /// of the range's min/max constraints. For example, consider we have the range min=0 and max=10, now let's give
    /// this range a snap value of 5 (which is the dead middle), the range will be divided in 3 possible values it can
    /// have: 0, 5 and 10 as it can only go in 5-step intervals. If the given value is crossing the bounds of an
    /// interval, the interval will be the new value, so for the result to be 5 the input value would have to be between
    /// `≥ 2.5` and `< 7.5`
    ///
    /// Check out this [Desmos graph](https://www.desmos.com/calculator/bskopovrf8) for testing different inputs.
    ///
    /// @param value The denormalised value to snap
    /// @return The snapped denormalised value
    public double snap(double value)
    {
        if (this.step > 0.0)
        {
            value = (this.min + (this.step * Math.floor((value - this.min) / this.step + 0.5)));
        }
        
        return this.clamp(value);
    }
}
