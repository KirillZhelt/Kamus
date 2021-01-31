package com.kamus.core.zookeeper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EndpointsZkAnnouncement extends BaseZkAnnouncement {

    private static final String ANNOUNCEMENTS_PATH = "/announcements/";
    private static final String MEMBER = "member_";

    private static final ObjectMapper mapper = new ObjectMapper();

    public EndpointsZkAnnouncement(String serviceName, Endpoints endpoints) throws JsonProcessingException {
        super(buildZkPath(serviceName), mapper.writeValueAsBytes(endpoints));
    }

    private static String buildZkPath(String serviceName) {
        return ANNOUNCEMENTS_PATH + serviceName + "/" + MEMBER;
    }

}
