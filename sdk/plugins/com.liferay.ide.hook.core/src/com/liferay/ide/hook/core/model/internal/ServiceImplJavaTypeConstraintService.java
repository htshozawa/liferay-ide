/*******************************************************************************
 * Copyright (c) 2000-2011 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * Contributors:
 * 		Gregory Amerson - initial implementation and ongoing maintenance
 *******************************************************************************/

package com.liferay.ide.hook.core.model.internal;

import com.liferay.ide.hook.core.model.ServiceWrapper;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.java.JavaTypeConstraint;
import org.eclipse.sapphire.java.JavaTypeConstraintBehavior;
import org.eclipse.sapphire.java.JavaTypeConstraintService;
import org.eclipse.sapphire.java.JavaTypeConstraintServiceData;
import org.eclipse.sapphire.java.JavaTypeKind;
import org.eclipse.sapphire.java.JavaTypeName;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.PropertyContentEvent;

/**
 * @author Gregory Amerson
 */
public class ServiceImplJavaTypeConstraintService extends JavaTypeConstraintService
{

    private Set<JavaTypeKind> kinds;

    private JavaTypeConstraintBehavior behavior;

    private ServiceWrapper service;

    @Override
    protected void initJavaTypeConstraintService()
    {
        super.initJavaTypeConstraintService();

        final ModelProperty property = context().find( ModelProperty.class );
        final JavaTypeConstraint javaTypeConstraintAnnotation = property.getAnnotation( JavaTypeConstraint.class );

        final Set<JavaTypeKind> kind = EnumSet.noneOf( JavaTypeKind.class );

        for( JavaTypeKind k : javaTypeConstraintAnnotation.kind() )
        {
            kind.add( k );
        }

        this.kinds = kind;

        this.behavior = javaTypeConstraintAnnotation.behavior();

        this.service = context( ServiceWrapper.class );

        Listener listener = new FilteredListener<PropertyContentEvent>()
        {

            @Override
            public void handleTypedEvent( PropertyContentEvent event )
            {
                refresh();
            }
        };

        this.service.attach( listener, "ServiceType" );
    }

    private Set<String> getServiceTypes()
    {
        JavaTypeName type = this.service.getServiceType().getContent( false );

        Set<String> types = new HashSet<String>();

        if( type != null )
        {
            types.add( type.qualified() + "Wrapper" );
        }

        return types;
    }

    @Override
    protected JavaTypeConstraintServiceData compute()
    {
        return new JavaTypeConstraintServiceData( this.kinds, getServiceTypes(), this.behavior );
    }

}
