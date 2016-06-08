package com.excilys.shooflers.dashboard.server.rest;

import com.excilys.shooflers.dashboard.server.dto.BundleMetadataDto;
import com.excilys.shooflers.dashboard.server.dto.mapper.BundleDtoMapperImpl;
import com.excilys.shooflers.dashboard.server.model.metadata.BundleMetadata;
import com.excilys.shooflers.dashboard.server.security.annotation.RequireValidApiKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RequireValidApiKey
@RestController
@RequestMapping(value = "/dashboard")
public class UpdateController {

    @Autowired
    private BundleDtoMapperImpl bundleMapper;

    @RequestMapping(method = RequestMethod.GET)
    public BundleMetadataDto getSlideshow() {
        return bundleMapper.toDto(new BundleMetadata.Builder().name("test").build());
    }

}