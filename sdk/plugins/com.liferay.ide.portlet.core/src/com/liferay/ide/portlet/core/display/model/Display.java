/*******************************************************************************
 * Copyright (c) 2000-2012 Liferay, Inc. All rights reserved.
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
 *      Kamesh Sampath - initial implementation
 *      Gregory Amerson - initial implementation review and ongoing maintenance
 *******************************************************************************/

package com.liferay.ide.portlet.core.display.model;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlDocumentType;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;

/**
 * @author Kamesh Sampath
 * @author Gregory Amerson
 */
@GenerateImpl
@XmlDocumentType
( 
    publicId = "-//Liferay//DTD Display 6.0.0//EN", 
    systemId = "http://www.liferay.com/dtd/liferay-display_6_0_0.dtd" 
)
@XmlBinding( path = "display" )
public interface Display extends IModelElement
{
    ModelElementType TYPE = new ModelElementType( Display.class );

    // *** Categories ***

    @Type( base = Category.class )
    @Label( standard = "Categories" )
    @XmlListBinding( mappings = { @XmlListBinding.Mapping( element = "category", type = Category.class ) } )
    ListProperty PROP_CATEGORIES = new ListProperty( TYPE, "Categories" );

    ModelElementList<Category> getCategories();

}
