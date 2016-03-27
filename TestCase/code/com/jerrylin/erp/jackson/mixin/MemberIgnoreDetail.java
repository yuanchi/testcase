package com.jerrylin.erp.jackson.mixin;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jerrylin.erp.model.VipDiscountDetail;

public interface MemberIgnoreDetail {
	@JsonIgnore public Set<VipDiscountDetail> getVipDiscountDetails();
}
