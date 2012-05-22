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
package org.openmrs.module.orderextension.api;

import java.util.Date;
import java.util.List;

import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.orderextension.DrugOrderSetMember;
import org.openmrs.module.orderextension.DrugRegimen;
import org.openmrs.module.orderextension.ExtendedDrugOrder;
import org.openmrs.module.orderextension.NestedOrderSetMember;
import org.openmrs.module.orderextension.OrderGroup;
import org.openmrs.module.orderextension.OrderSet;
import org.openmrs.module.orderextension.OrderSetMember;
import org.openmrs.module.orderextension.api.db.OrderExtensionDAO;
import org.openmrs.module.orderextension.util.OrderExtensionUtil;
import org.springframework.transaction.annotation.Transactional;

/**
 * Core implementation of the OrderExensionService
 */
@Transactional
public class OrderExtensionServiceImpl extends BaseOpenmrsService implements OrderExtensionService {
	
	private OrderExtensionDAO dao;

	/**
	 * @see OrderExtensionService#getOrderSet(Integer)
	 */
	@Override
	@Transactional(readOnly=true)
	public OrderSet getOrderSet(Integer id) {
		return dao.getOrderSet(id);
	}

	/**
	 * @see OrderExtensionService#getOrderSetByUuid(String)
	 */
	@Override
	@Transactional(readOnly=true)
	public OrderSet getOrderSetByUuid(String uuid) {
		return dao.getOrderSetByUuid(uuid);
	}

	/**
	 * @see OrderExtensionService#getAllOrderSets(boolean)
	 */
	@Override
	@Transactional(readOnly=true)
	public List<OrderSet> getNamedOrderSets(boolean includeRetired) {
		return dao.getNamedOrderSets(null, null, includeRetired);
	}

	/**
	 * @see OrderExtensionService#findOrderSets(String, Concept)
	 */
	@Override
	@Transactional(readOnly=true)
	public List<OrderSet> findAvailableOrderSets(String partialName, Concept indication) {
		return dao.getNamedOrderSets(partialName, indication, false);
	}

	/**
	 * @see OrderExtensionService#saveOrderSet(OrderSet)
	 */
	@Override
	@Transactional
	public OrderSet saveOrderSet(OrderSet orderSet) {
		return dao.saveOrderSet(orderSet);
	}

	/**
	 * @see OrderExtensionService#purgeOrderSet(OrderSet)
	 */
	@Override
	@Transactional
	public void purgeOrderSet(OrderSet orderSet) {
		dao.purgeOrderSet(orderSet);
	}

	/**
	 * @see OrderExtensionService#getOrderSetMember(Integer)
	 */
	@Override
	@Transactional(readOnly=true)
	public OrderSetMember getOrderSetMember(Integer id) {
		return dao.getOrderSetMember(id);
	}

	/**
	 * @see OrderExtensionService#getParentOrderSets(OrderSet)
	 */
	@Override
	@Transactional(readOnly=true)
	public List<OrderSet> getParentOrderSets(OrderSet orderSet) {
		return dao.getParentOrderSets(orderSet);
	}

	/**
	 * @see OrderExtensionService#saveOrderGroup(OrderGroup)
	 */
	@Override
	@Transactional
	public <T extends OrderGroup> T saveOrderGroup(T orderGroup) {
		return dao.saveOrderGroup(orderGroup);
	}

	/**
	 * @see OrderExtensionService#getOrderGroups(Patient, Class)
	 */
	@Override
	@Transactional(readOnly=true)
	public <T extends OrderGroup> List<T> getOrderGroups(Patient patient, Class<T> type) {
		return dao.getOrderGroups(patient, type);
	}

	/**
	 * @see OrderExtensionService#addOrdersForPatient(Patient, OrderSet, Date, Integer)
	 */
	@Override
	@Transactional
	public void addOrdersForPatient(Patient patient, OrderSet orderSet, Date startDate, Integer numCycles) {

		OrderExtensionService orderExtSvc = Context.getService(OrderExtensionService.class);
		
		if (numCycles == null) {
			numCycles = 1;
		}
		
		for (int i=0; i<numCycles; i++) {
			
			DrugRegimen orderGroup = new DrugRegimen();
			orderGroup.setOrderSet(orderSet);
			if (orderSet.isCyclical()) {
				orderGroup.setCycleNumber(i+1);
			}
			
			Date cycleStart = startDate;

			if (orderSet.getCycleLengthInDays() != null) {
				cycleStart = OrderExtensionUtil.incrementDate(cycleStart, orderSet.getCycleLengthInDays() * i);
			}
			for (OrderSetMember member : orderSet.getMembers()) {
				
				Date memberStartDate = cycleStart;
				Date memberEndDate = null;
				if (member.getRelativeStartDay() != null) {
					memberStartDate = OrderExtensionUtil.incrementDate(memberStartDate, member.getRelativeStartDay() - 1);
				}
				if (member.getLengthInDays() != null) {
					memberEndDate = OrderExtensionUtil.incrementDate(memberStartDate, member.getLengthInDays() - 1);
				}
				
				if (member instanceof NestedOrderSetMember) {
					NestedOrderSetMember nestedMember = (NestedOrderSetMember)member;
					addOrdersForPatient(patient, nestedMember.getNestedOrderSet(), memberStartDate, null);
				}
				else if (member instanceof DrugOrderSetMember) {
					DrugOrderSetMember drugMember = (DrugOrderSetMember)member;
					ExtendedDrugOrder drugOrder = new ExtendedDrugOrder();
					drugOrder.setOrderType(drugMember.getOrderType());
					drugOrder.setConcept(drugMember.getConcept());
					drugOrder.setDrug(drugMember.getDrug());
					drugOrder.setDose(drugMember.getDose());
					drugOrder.setUnits(drugMember.getUnits());
					drugOrder.setRoute(drugMember.getRoute());
					drugOrder.setAdministrationInstructions(drugMember.getAdministrationInstructions());
					drugOrder.setFrequency(drugMember.getFrequency());
					drugOrder.setPrn(drugMember.isAsNeeded());
					drugOrder.setInstructions(drugMember.getInstructions());
					Concept indication = drugMember.getIndication();
					if (indication == null) {
						indication = orderSet.getIndication();
					}
					drugOrder.setIndication(indication);
					drugOrder.setStartDate(memberStartDate);
					drugOrder.setAutoExpireDate(memberEndDate);
					drugOrder.setGroup(orderGroup);
					drugOrder.setPatient(patient);
					// TODO:  Add this to a new encounter ?
					orderGroup.addMember(drugOrder);
				}
				else {
					throw new APIException("We do not yet handle TestOrders");
				}
			}
			
			orderExtSvc.saveOrderGroup(orderGroup);
		}
	}

	/**
	 * @return the dao
	 */
	public OrderExtensionDAO getDao() {
		return dao;
	}

	/**
	 * @param dao the dao to set
	 */
	public void setDao(OrderExtensionDAO dao) {
		this.dao = dao;
	}
}
