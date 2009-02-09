package org.hibersap.mapping;

/*
 * Copyright (C) 2008 akquinet tech@spree GmbH
 * 
 * This file is part of Hibersap.
 * 
 * Hibersap is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * Hibersap is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with Hibersap. If
 * not, see <http://www.gnu.org/licenses/>.
 */

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

import org.hibersap.HibersapException;

/**
 * @author Carsten Erker
 */
public class ReflectionHelper
{
    /**
     * Get the array type of type, or null if type is not an array.
     * 
     * @param type
     * @return
     */
    public static Class<?> getArrayType( Class<?> type )
    {
        if ( type.isArray() )
        {
            return getClass( type.getComponentType() );
        }
        return null;
    }

    /**
     * Get the underlying class for a type, or null if the type is a variable type. Stolen from:
     * http://www.artima.com/weblogs/viewpost.jsp?thread=208860
     * 
     * @param type the type
     * @return the underlying class
     */
    private static Class<?> getClass( Type type )
    {
        if ( type instanceof Class )
        {
            return (Class<?>) type;
        }
        else if ( type instanceof ParameterizedType )
        {
            return getClass( ( (ParameterizedType) type ).getRawType() );
        }
        else if ( type instanceof GenericArrayType )
        {
            Type componentType = ( (GenericArrayType) type ).getGenericComponentType();
            Class<?> componentClass = getClass( componentType );
            if ( componentClass != null )
            {
                return Array.newInstance( componentClass, 0 ).getClass();
            }
            else
            {
                return null;
            }
        }
        else
        {
            return null;
        }
    }

    public static Field getDeclaredField( Object bean, String fieldName )
    {
        Class<?> clazz = bean.getClass();
        try
        {
            java.lang.reflect.Field declaredField = clazz.getDeclaredField( fieldName );
            return declaredField;
        }
        catch ( SecurityException e )
        {
            throw new HibersapException( "Field " + bean.getClass() + "." + fieldName + " is not accessible", e );
        }
        catch ( NoSuchFieldException e )
        {
            throw new HibersapException( "Field " + bean.getClass() + "." + fieldName + " does not exist in class "
                + clazz, e );
        }
    }

    public static Object getFieldValue( Object bean, String fieldName )
    {
        try
        {
            java.lang.reflect.Field javaField = getDeclaredField( bean, fieldName );
            javaField.setAccessible( true );
            return javaField.get( bean );
        }
        catch ( IllegalArgumentException e )
        {
            throw new HibersapException( e );
        }
        catch ( IllegalAccessException e )
        {
            throw new HibersapException( "Field " + bean.getClass() + "." + fieldName + " is not accessible", e );
        }
    }

    /**
     * Get the actual type of a one-dimensional generic field.
     * 
     * @param field
     * @return
     */
    public static Class<?> getGenericType( Field field )
    {
        Type genericType = field.getGenericType();
        return getGenericType( genericType );
    }

    public static Class<?> getGenericType( Type genericType )
    {
        if ( genericType instanceof ParameterizedType )
        {
            ParameterizedType paramType = (ParameterizedType) genericType;
            Type[] actualTypeArguments = paramType.getActualTypeArguments();
            if ( actualTypeArguments.length == 1 )
            {
                Type actualType = actualTypeArguments[0];
                if ( actualType != null )
                {
                    return getClass( actualType );
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static Collection<Object> newCollectionInstance( Class<? extends Collection> clazz )
    {
        try
        {
            return clazz.newInstance();
        }
        catch ( InstantiationException e )
        {
            throw new HibersapException( "Can not create an instance of type " + clazz.getName(), e );
        }
        catch ( IllegalAccessException e )
        {
            throw new HibersapException( "Can not create an instance of type " + clazz.getName(), e );
        }
    }

    public static Object newInstance( Class<? extends Object> clazz )
    {
        try
        {
            Constructor<? extends Object> defaultConstructor = clazz.getDeclaredConstructor();
            defaultConstructor.setAccessible( true );
            return defaultConstructor.newInstance();
        }
        // TODO add meaningful message to exceptions
        catch ( InstantiationException e )
        {
            throw new HibersapException( "Can not create an instance of type " + clazz.getName(), e );
        }
        catch ( IllegalAccessException e )
        {
            throw new HibersapException( "Can not create an instance of type " + clazz.getName(), e );
        }
        catch ( NoSuchMethodException e )
        {
            throw new HibersapException( "Class does not have a default constructor: " + clazz.getName(), e );
        }
        catch ( InvocationTargetException e )
        {
            throw new HibersapException( "Can not create an instance of type " + clazz.getName(), e );
        }
    }

    public static void setFieldValue( Object bean, String fieldName, Object value )
    {
        if ( value == null )
        {
            throw new HibersapException( "Cannot set null value on field " + fieldName + " of bean "
                + bean.getClass().getName() );
        }

        try
        {
            java.lang.reflect.Field declaredField = bean.getClass().getDeclaredField( fieldName );
            declaredField.setAccessible( true );
            declaredField.set( bean, value );
        }
        catch ( SecurityException e )
        {
            throw new HibersapException( "Can not assign an object of type " + value.getClass().getName()
                + " to the field " + bean.getClass().getName() + "." + fieldName, e );
        }
        catch ( NoSuchFieldException e )
        {
            throw new HibersapException( "Can not assign an object of type " + value.getClass().getName()
                + " to the field " + bean.getClass().getName() + "." + fieldName, e );
        }
        catch ( IllegalArgumentException e )
        {
            throw new HibersapException( "Can not assign an object of type " + value.getClass().getName()
                + " to the field " + bean.getClass().getName() + "." + fieldName, e );
        }
        catch ( IllegalAccessException e )
        {
            throw new HibersapException( "Can not assign an object of type " + value.getClass().getName()
                + " to the field " + bean.getClass().getName() + "." + fieldName, e );
        }
    }

    private ReflectionHelper()
    {
        // should not be instantiated
    }
}