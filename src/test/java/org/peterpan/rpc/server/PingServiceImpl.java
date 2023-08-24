package org.peterpan.rpc.server;

import org.peterpan.rpc.IDL.Ping.PingRequest;
import org.peterpan.rpc.IDL.Ping.PingResponse;
import org.peterpan.rpc.IDL.Ping.PingService;

public class PingServiceImpl implements PingService {

    @Override
    public PingResponse ping(PingRequest request) {
        String name = request.getName();
        String retMsg = "pong: " + name;
        PingResponse response = new PingResponse(retMsg);
        return response;
    }
}
