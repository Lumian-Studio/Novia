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

import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenAxis;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.navigation.ScreenRectangle;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;



//**********************************************************************************************************************
/// Describes the natural navigation order that is used by default to navigate between components on screen.
public class NaturalNavigator
    implements IComponentNavigator
{
    //******************************************************************************************************************
    public static final NaturalNavigator INSTANCE = new NaturalNavigator();
    
    //------------------------------------------------------------------------------------------------------------------
    private static final Comparator<GuiComponent>       NATURAL_TAB_COMPARATOR;
    private static final List<Comparator<GuiComponent>> NATURAL_ARROW_COMPARATORS;
    
    //==================================================================================================================
    static
    {
        NATURAL_TAB_COMPARATOR    = Comparator
            .comparingInt(GuiComponent::getNavigationOrder)
            .thenComparing(component1 -> !component1.isPinned());
        NATURAL_ARROW_COMPARATORS = Arrays
            .stream(ScreenDirection.values())
            .map(direction ->
            {
                final ScreenDirection other_direction = direction.getAxis().orthogonal().getPositive();
                
                return Comparator
                    .<GuiComponent, Integer>comparing(
                        (child -> child.getScreenRect().getBoundInDirection(direction.getOpposite())),
                        direction.coordinateValueComparator())
                    .thenComparing(
                        (child -> child.getScreenRect().getBoundInDirection(other_direction.getOpposite())),
                        other_direction.coordinateValueComparator());
            })
            .toList();
    }
    
    //******************************************************************************************************************
    public static NaturalNavigator withComponents(final Collection<GuiComponent> components)
    {
        return new NaturalNavigator()
        {
            //**********************************************************************************************************
            @Override
            public List<GuiComponent> getAll(final GuiComponent container, final FocusNavigationEvent nav)
            {
                return List.copyOf(components);
            }
        };
    }
    
    //------------------------------------------------------------------------------------------------------------------
    protected static Stream<GuiComponent> getAllSorted(final GuiComponent             container,
                                                       final Comparator<GuiComponent> comparator,
                                                       final FocusNavigationEvent     nav)
    {
        return container
            .getChildren()
            .stream()
            .filter(component -> component.isVisible() && component.hasActiveFlag())
            .sorted(comparator)
            .flatMap(child ->
            {
                final IComponentNavigator child_navigator = child.getNavigator();
                return Stream.concat(
                    (child.canBeNavigatedTo() ? Stream.of(child) : Stream.empty()),
                    (child_navigator != null ? child_navigator.getAll(child, nav).stream() : Stream.empty()));
            });
    }
    
    //******************************************************************************************************************
    protected NaturalNavigator() {}
    
    //==================================================================================================================
    @Override
    public Optional<GuiComponent> findNext(final GuiComponent container, final FocusNavigationEvent nav)
    {
        if (!container.hasFocus())
        {
            return Optional.empty();
        }
        
        final List<GuiComponent> all = this.getAll(container, nav);
        
        if (all.isEmpty())
        {
            return Optional.empty();
        }
        
        int focused_index = -1;
        
        for (int i = 0; i < all.size(); i++)
        {
            if (all.get(i).isFocused())
            {
                focused_index = i;
                break;
            }
        }
        
        if (nav instanceof FocusNavigationEvent.TabNavigation(final boolean forward))
        {
            final int index = (forward ? (focused_index + 1) : (focused_index - 1));
            return (index >= 0 && index < all.size() ? Optional.of(all.get(index)) : Optional.empty());
        }
        
        final GuiComponent    focused    = all.get(focused_index);
        final ScreenDirection dir        = (nav instanceof FocusNavigationEvent.ArrowNavigation arrow
            ? arrow.direction()
            : ScreenDirection.DOWN);
        final ScreenRectangle focus_rect = focused.getScreenRect();
        final ScreenAxis      other_axis = dir.getAxis().orthogonal();
		final int             focus_pos  = focus_rect.getBoundInDirection(dir.getOpposite());
        
        return all
            .stream()
            .filter(child ->
            {
                final ScreenRectangle child_rect = child.getScreenRect();

                if (!child_rect.overlapsInAxis(focus_rect, other_axis) || child == focused)
                {
                    return false;
                }
                
                final int child_pos = child_rect.getBoundInDirection(dir.getOpposite());
                return (
                    dir.isAfter(child_pos, focus_pos)
                    || (
                        child_pos == focus_pos
                        && dir.isAfter(child_rect.getBoundInDirection(dir), focus_rect .getBoundInDirection(dir))
                    )
                );
            })
            .findFirst();
    }
    
    @Override
    public Optional<GuiComponent> findFirst(final GuiComponent container, final FocusNavigationEvent nav)
    {
        return this.getAll(container, nav).stream().findFirst();
    }
    
    @Override
    public List<GuiComponent> getAll(final GuiComponent container, final FocusNavigationEvent nav)
    {
        return NaturalNavigator
            .getAllSorted(
                container,
                (switch (nav)
                {
                    case FocusNavigationEvent.TabNavigation   ignored -> NaturalNavigator.NATURAL_TAB_COMPARATOR;
                    case FocusNavigationEvent.ArrowNavigation arrow   -> NaturalNavigator.NATURAL_ARROW_COMPARATORS
                                                                             .get(arrow.direction().ordinal());
                    case FocusNavigationEvent.InitialFocus    ignored -> NaturalNavigator.NATURAL_ARROW_COMPARATORS
                                                                             .get(ScreenDirection.DOWN.ordinal());
                    
                    default -> throw new UnsupportedOperationException("Unsupported navigation type: " + nav);
                }),
                nav)
            .collect(Collectors.toList());
    }
}
