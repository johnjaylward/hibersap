package org.hibersap.execution.jco;

/*
 * Copyright (C) 2008-2009 akquinet tech@spree GmbH
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
import java.util.Enumeration;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibersap.HibersapException;
import org.hibersap.configuration.HibersapProperties;
import org.hibersap.execution.Connection;
import org.hibersap.session.Context;

/**
 * Uses the SAP Java Connector to connect to SAP.
 * 
 * @author Carsten Erker
 */
public class JCoContext
    implements Context
{
    private static final long serialVersionUID = 1L;

    private static final Log LOG = LogFactory.getLog( JCoContext.class );

    private static final String JCO_PROPERTIES_PREFIX = "jco.";

    private String destinationName;

    /**
     * {@inheritDoc}
     */
    public void configure( final Properties props )
        throws HibersapException
    {
        LOG.trace( "configure JCo context" );

        final Properties jcoProperties = new Properties();
        Enumeration<?> keys = props.propertyNames();
        while ( keys.hasMoreElements() )
        {
            String key = (String) keys.nextElement();
            if ( key.startsWith( JCO_PROPERTIES_PREFIX ) )
            {
                Object value = props.getProperty( key );
                jcoProperties.put( key, value );
            }
        }

        destinationName = props.getProperty( HibersapProperties.SESSION_FACTORY_NAME );
        if ( StringUtils.isEmpty( destinationName ) )
        {
            throw new HibersapException( "A session factory name must be specified in property "
                + HibersapProperties.SESSION_FACTORY_NAME );
        }

        JCoEnvironment.registerDestination( destinationName, jcoProperties );
    }

    /**
     * {@inheritDoc}
     */
    public void reset()
    {
        JCoEnvironment.unregisterDestination( destinationName );
        destinationName = null;
    }

    /**
     * {@inheritDoc}
     */
    public Connection getConnection()
    {
        return new JCoConnection( destinationName );
    }
}