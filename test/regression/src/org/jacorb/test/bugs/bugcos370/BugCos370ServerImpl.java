package org.jacorb.test.bugs.bugcos370;

import org.jacorb.test.orb.AnyServerPOA;
import org.omg.CORBA.Any;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.TypeCode;

/**
 * @author Alphonse Bendt
 * @version $Id: BugCos370ServerImpl.java,v 1.1 2006-11-27 14:45:19 alphonse.bendt Exp $
 */
public class BugCos370ServerImpl extends AnyServerPOA
{
    public Any bounce_any(Any myAny)
    {
        TypeCode type = myAny.type();

        if (!type.equivalent(NamingAttributes_THelper.type()))
        {
            throw new BAD_PARAM("types are not equivalent: " + type);
        }

        if (!type.equal(NamingAttributes_THelper.type()))
        {
            throw new BAD_PARAM("types are not equal: " + type +  " <> " + NamingAttributes_THelper.type());
        }

        return myAny;
    }
}
