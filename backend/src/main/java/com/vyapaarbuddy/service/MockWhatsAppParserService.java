package com.vyapaarbuddy.service;

import com.vyapaarbuddy.dto.request.MockWhatsAppRequest;
import com.vyapaarbuddy.dto.response.MockCommandResponse;

public interface MockWhatsAppParserService {

    MockCommandResponse parseMessage(MockWhatsAppRequest request);

    MockCommandResponse executeMessage(MockWhatsAppRequest request);
}
