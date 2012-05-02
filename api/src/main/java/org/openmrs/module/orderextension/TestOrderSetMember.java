/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.orderextension;

import org.apache.commons.lang.StringUtils;
import org.openmrs.OrderType;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;

/**
 * This extends the capabilities of the SingleOrderSetMember to provide additional means 
 * for specifying a pre-defined TestOrder
 */
public class TestOrderSetMember extends SingleOrderSetMember {
	
	public static final long serialVersionUID = 1L;
	
	/**
	 * Default Constructor
	 */
	public TestOrderSetMember() {}

	/**
	 * @see SingleOrderSetMember#getOrderType()
	 */
	@Override
	public OrderType getOrderType() {
		OrderType type = null;
		String gp = Context.getAdministrationService().getGlobalProperty("orderextension.testOrderType");
		if (StringUtils.isNotBlank(gp)) {
			type = Context.getOrderService().getOrderTypeByUuid(gp);
			if (type == null) {
				type = Context.getOrderService().getOrderType(Integer.parseInt(gp));
			}
		}
		if (type == null) {
			throw new APIException("Invalid global property value for orderextension.testOrderType");
		}
		return type;
	}
}
