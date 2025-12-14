package com.sbaldasso.b2c_subscription_service.domain.model;

import lombok.Getter;
import lombok.Setter;

@Getter
public class UserId {

    private Long id;

    public UserId(){

    }

    public UserId(Long id){
        this.id = id;
    }

}
