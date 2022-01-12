package com.bowen.shop.service;

import com.bowen.shop.generate.ShopMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ShopService {
    private ShopMapper shopMapper;

    @Autowired
    @SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"}, justification = "I prefer to suppress these FindBugs warnings")
    public ShopService(ShopMapper shopMapper) {
        this.shopMapper = shopMapper;
    }
}
