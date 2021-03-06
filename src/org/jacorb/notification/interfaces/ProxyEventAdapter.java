/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Library General Public
 *   License as published by the Free Software Foundation; either
 *   version 2 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this library; if not, write to the Free
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */

package org.jacorb.notification.interfaces;

import org.omg.CosNotifyChannelAdmin.AdminLimitExceeded;

/**
 * @author Alphonse Bendt
 * @version $Id: ProxyEventAdapter.java,v 1.1 2006-01-10 23:00:48 alphonse.bendt Exp $
 */
public abstract class ProxyEventAdapter implements ProxyEventListener
{
    public void actionProxyCreationRequest(ProxyEvent event) throws AdminLimitExceeded
    {
        // no op
    }

    public void actionProxyDisposed(ProxyEvent event)
    {
        // no op
    }

    public void actionProxyCreated(ProxyEvent event)
    {
        // no op
    }
}
