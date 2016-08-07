package av.nado.register;

import java.util.Map;

import av.nado.remote.NadoProxy;
import av.util.exception.AException;

public interface RegisterNotify
{
    public void onRegisterNotify(Map<String, NadoProxy> mapProxy) throws AException;
}
