package rpc.peterpan.com.server;

import rpc.peterpan.com.IDL.Ping.PingRequest;
import rpc.peterpan.com.IDL.Ping.PingResponse;
import rpc.peterpan.com.IDL.Ping.PingService;

public class PingServiceImpl implements PingService {

    @Override
    public PingResponse ping(PingRequest request) {
        String name = request.getName();
        String retMsg = "pong: " + name;
        PingResponse response = new PingResponse(retMsg);
        return response;
    }
}
