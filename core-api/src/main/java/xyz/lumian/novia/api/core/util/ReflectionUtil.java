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

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.*;
import java.util.Arrays;



//**********************************************************************************************************************
/// A utility class that helps to abstract away some vexing but very specific reflection algorithms.
public class ReflectionUtil
{
    //******************************************************************************************************************
    /// Tries to get a Class object for the nth generic type of the given class' super class.
    /// @param clazz The class implementing the parametrised super class
    /// @param index The index of the generic type in the generic parameter list
    /// @return A Class object for the nth type parameter or null if there was a problem
    /// @param <T> The type of the class implementing the parametrised super class
    /// @param <U> The type parameter of the resulting Class object
    /// @throws ReflectionException If there was no superclass, or the generic parameter could not be cast to T
    @SuppressWarnings("unchecked")
    public static <T, U> @Nullable Class<T> getGenericSuperTypeParameter(final @Nullable Class<U> clazz,
                                                                         final int                index)
        throws ReflectionException
    {
        if (clazz == null)
        {
            return null;
        }

        try
        {
            final ParameterizedType super_type = (ParameterizedType) clazz.getGenericSuperclass();
            return (Class<T>) super_type.getActualTypeArguments()[index];
        }
        catch (TypeNotPresentException | ClassCastException ex)
        {
            throw new ReflectionException(ex);
        }
    }

    /// Tries to instantiate a class from the given arguments.
    /// @param clazz The class to be instantiated
    /// @param args  The constructor arguments to instantiate the class with
    /// @return A new instance of the given class
    /// @param <T> The class type
    /// @throws ReflectionException If no constructor with the given arguments could be found, the constructor was
    ///                             not accessible, if the class is abstract or an interface or if the constructor
    ///                             threw an exception
    public static <T> @Nullable T instantiateClass(final @Nullable Class<T> clazz, final Object ...args)
        throws ReflectionException
    {
        if (clazz == null)
        {
            return null;
        }

        try
        {
            return clazz.getConstructor(Arrays
                .stream(args)
                .map(Object::getClass)
                .toArray(Class[]::new)
            ).newInstance(args);
        }
        catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException ex)
        {
            throw new ReflectionException(ex);
        }
    }

    /// Tries to set the value of a field of an object, either static or instance bound. Other than
    /// [ReflectionUtil#setValue(Field, Object, Object)], this will also try to set non-public and final fields.
    ///
    /// @param field  The field to set
    /// @param value  The value to assign to it
    /// @param target The target object the field is a member of, or null if the field is static
    public static void forceSetValue(final Field field, final @Nullable Object value, final @Nullable Object target)
        throws ReflectionException
    {
        try
        {
            field.setAccessible(true);

            final Field modifiers = Field.class.getDeclaredField("modifiers");
            modifiers.setAccessible(true);
            modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);

            field.set(target, value);
        }
        catch (IllegalAccessException | InaccessibleObjectException | SecurityException | NoSuchFieldException ex)
        {
            throw new ReflectionException(String.format("could not set field '%s'", field.getName()), ex);
        }
    }

    /// Tries to set the value of a field of an object, either static or instance bound. Other than
    /// [ReflectionUtil#forceSetValue(Field, Object, Object)], this will fail if the field is not public or final.
    ///
    /// @param field  The field to set
    /// @param value  The value to assign to it
    /// @param target The target object the field is a member of, or null if the field is static
    public static void setValue(final Field field, final Object value, final Object target)
        throws ReflectionException
    {
        try
        {
            field.set(target, value);
        }
        catch (IllegalAccessException | InaccessibleObjectException | SecurityException ex)
        {
            throw new ReflectionException(String.format("could not set field '%s'", field.getName()), ex);
        }
    }
}
