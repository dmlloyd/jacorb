package demo.interceptors;

import org.omg.PortableInterceptor.*;
import org.omg.CosNaming.*;
import org.omg.CORBA.Any;

/**
 * This interceptor will silently redirect requests of a 
 * client to another target by throwing a ForwardRequest 
 * exception.
 *
 * @author Nicolas Noffke
 */

public class ClientForwardInterceptor
    extends org.omg.CORBA.LocalObject 
    implements ClientRequestInterceptor
{
    private MyServer grid = null;
    private boolean in_loop = false;

    public ClientForwardInterceptor(NamingContextExt nc) 
    {
        try
        {
            grid = MyServerHelper.narrow(nc.resolve(nc.to_name("grid2.example")));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public String name() 
    {
        return "ClientForwardInterceptor";
    }

    public void destroy()
    {

    }

    /**
     * Throws a ForwardRequest, if target is wrong
     */
    public void send_request(ClientRequestInfo ri) 
        throws ForwardRequest
    {
        // loop prevention, because _is_a will also land here
        if (! in_loop)
        {
            in_loop = true;

            if (ri.effective_target()._is_a(MyServerHelper.id()))
            {
                if (! grid._is_equivalent(ri.effective_target()))
                {
                    System.out.println("Interceptor: Throwing ForwardRequest");
          
                    // setting in_loop not back, since the forward is permanent
                    throw new ForwardRequest( grid );
                }
                else
                {
                    System.out.println("Interceptor: target is ok");
                    in_loop = false;
                }
            }
            else
            {
                System.out.println("Interceptor: ignoring, target has wrong type");
                in_loop = false;
            }
        }
        else
            System.out.println("Interceptor: ignoring, loop prevention");
    }

    public void send_poll(ClientRequestInfo ri){
    }

    public void receive_reply(ClientRequestInfo ri){
    }

    public void receive_exception(ClientRequestInfo ri) 
        throws ForwardRequest{
    }

    public void receive_other(ClientRequestInfo ri) 
        throws ForwardRequest{
    }
} // ClientForwardInterceptor
