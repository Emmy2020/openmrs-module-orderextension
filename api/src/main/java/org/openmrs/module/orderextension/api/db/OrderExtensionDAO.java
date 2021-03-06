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
package org.openmrs.module.orderextension.api.db;

import java.util.Date;
import java.util.List;

import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.module.orderextension.DrugRegimen;
import org.openmrs.module.orderextension.ExtendedDrugOrder;
import org.openmrs.module.orderextension.OrderGroup;
import org.openmrs.module.orderextension.OrderSet;
import org.openmrs.module.orderextension.OrderSetMember;
import org.openmrs.module.orderextension.api.OrderExtensionService;

/**
 * Order Extension Data Access Interface
 */
public interface OrderExtensionDAO {
	
	/**
	 * @see OrderExtensionService#getOrderSet(Integer)
	 */
	public OrderSet getOrderSet(Integer id);
	
	/**
	 * @see OrderExtensionService#getOrderSetByUuid(String)
	 */
	public OrderSet getOrderSetByUuid(String uuid);
	
	/**
	 * Return all OrderSets that have a non-null name property, 
	 * whose name matches the partialName if specified, whose indication matches the indication if specified,
	 * and which are not retired, if specified
	 */
	public List<OrderSet> getNamedOrderSets(String partialName, Concept indication, boolean includeRetired);
	
	/**
	 * @see OrderExtensionService#saveOrderSet(OrderSet)
	 */
	public OrderSet saveOrderSet(OrderSet orderSet);
	
	/**
	 * @see OrderExtensionService#purgeOrderSet(OrderSet)
	 */
	public void purgeOrderSet(OrderSet orderSet);
	
	/**
	 * @see OrderExtensionService#getOrderSetMember(OrderSetMember)
	 */
	public OrderSetMember getOrderSetMember(Integer id);
	
	/**
	 * @see OrderExtensionService#getParentOrderSets(OrderSet)
	 */
	public List<OrderSet> getParentOrderSets(OrderSet orderSet);
	
	/**
	 * Persists the passed OrderGroup to the database
	 */
	public <T extends OrderGroup> T saveOrderGroup(T orderGroup);
	
	/**
	 * @see OrderExtensionService#getOrderGroups(Patient, Class)
	 */
	public <T extends OrderGroup> List<T> getOrderGroups(Patient patient, Class<T> type);
	
	/**
	 * @see OrderExtensionService#getOrderGroup(Integer id)
	 */
	public OrderGroup getOrderGroup(Integer id);

    /**
     * @see OrderExtensionService#getDrugRegimen(Integer id)
     */
    public DrugRegimen getDrugRegimen(Integer id);

    /**
     * @see OrderExtensionService#getExtendedDrugOrdersForPatient(Patient patient)
     */
    public List<ExtendedDrugOrder> getExtendedDrugOrdersForPatient(Patient patient, Concept indication, Date startDateAfter, Date startDateBefore);

	/**
     * @see OrderExtensionService#getMaxNumberOfCyclesForRegimen(DrugRegimen regimen)
     */
    public Integer getMaxNumberOfCyclesForRegimen(Patient patient, DrugRegimen regimen);
    
}

