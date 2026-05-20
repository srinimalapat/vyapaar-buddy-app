package com.vyapaarbuddy.service;

import com.vyapaarbuddy.dto.request.BusinessRequest;
import com.vyapaarbuddy.dto.response.BusinessResponse;

public interface BusinessService {

    BusinessResponse createBusiness(BusinessRequest request);

    BusinessResponse getMyBusiness();

    BusinessResponse updateMyBusiness(BusinessRequest request);
}
