package com.jerrylin.erp.security.extend;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.jerrylin.erp.model.PersonalInfo;

@Entity
@DiscriminatorValue("1")
public class UserInfo extends PersonalInfo {

}
