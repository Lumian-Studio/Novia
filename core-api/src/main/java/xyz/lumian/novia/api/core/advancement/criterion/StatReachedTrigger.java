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
package xyz.lumian.novia.api.core.advancement.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;

import java.util.Optional;



//**********************************************************************************************************************
public class StatReachedTrigger
    extends SimpleCriterionTrigger<StatReachedTrigger.TriggerInstance>
{
    //******************************************************************************************************************
    public record TriggerInstance(
        Optional<ContextAwarePredicate> player,
        Identifier                      stat,
        MinMaxBounds.Ints               value
    ) implements SimpleCriterionTrigger.SimpleInstance
    {
        //**************************************************************************************************************
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                EntityPredicate.ADVANCEMENT_CODEC
                    .optionalFieldOf("player")
                    .forGetter(TriggerInstance::player),
                Stats.CUSTOM.getRegistry().byNameCodec()
                    .fieldOf("stat")
                    .forGetter(TriggerInstance::stat),
                MinMaxBounds.Ints.CODEC
                    .fieldOf("value")
                    .forGetter(TriggerInstance::value))
            .apply(instance, TriggerInstance::new));

        //**************************************************************************************************************
        /// Creates a [StatReachedTrigger] for the given id and the given value range.
        /// @param id The [Identifier] of the stat
        /// @param value The integer range that this criterion should wait for
        public static Criterion<TriggerInstance> forStat(final Identifier id, final MinMaxBounds.Ints value)
        {
            return NoviaCriteriaTriggers.STAT_REACHED.createCriterion(new TriggerInstance(Optional.empty(), id, value));
        }
        
        //**************************************************************************************************************
        public boolean matches(final Stat<?> stat, final int currentValue)
        {
            final Stat<Identifier> reg_stat = Stats.CUSTOM.get(this.stat);
            return (stat == reg_stat && this.value.matches(currentValue));
        }
    }

    //******************************************************************************************************************
    @Override public Codec<TriggerInstance> codec() { return StatReachedTrigger.TriggerInstance.CODEC; }
    
    //==================================================================================================================
    public void trigger(final ServerPlayer player, final Stat<?> stat, final int currentValue)
    {
        this.trigger(player, conditions -> conditions.matches(stat, currentValue));
    }
}
