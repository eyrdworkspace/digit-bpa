/*
 * eGov suite of products aim to improve the internal efficiency,transparency,
 *    accountability and the service delivery of the government  organizations.
 *
 *     Copyright (C) <2015>  eGovernments Foundation
 *
 *     The updated version of eGov suite of products as by eGovernments Foundation
 *     is available at http://www.egovernments.org
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see http://www.gnu.org/licenses/ or
 *     http://www.gnu.org/licenses/gpl.html .
 *
 *     In addition to the terms of the GPL license to be adhered to in using this
 *     program, the following additional terms are to be complied with:
 *
 *         1) All versions of this program, verbatim or modified must carry this
 *            Legal Notice.
 *
 *         2) Any misrepresentation of the origin of the material is prohibited. It
 *            is required that all modified versions of this material be marked in
 *            reasonable ways as different from the original version.
 *
 *         3) This license does not grant any rights to any user of the program
 *            with regards to rights under trademark law for use of the trade names
 *            or trademarks of eGovernments Foundation.
 *
 *   In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 */
package org.egov.bpa.master.service;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.egov.bpa.master.entity.BpaFeeMapping;
import org.egov.bpa.master.entity.enums.FeeApplicationType;
import org.egov.bpa.master.entity.enums.FeeSubType;
import org.egov.bpa.master.repository.BpaFeeMappingRepository;
import org.egov.bpa.transaction.entity.BpaApplication;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class BpaFeeMappingService {

	@Autowired
	private BpaFeeMappingRepository bpaFeeMappingRepository;
	@Autowired
	private ApplicationSubTypeService applicationSubTypeService;
	@PersistenceContext
	private EntityManager entityManager;

	public Session getCurrentSession() {
		return entityManager.unwrap(Session.class);
	}

	public List<BpaFeeMapping> findAll() {
		return bpaFeeMappingRepository.findAll();
	}

	public BpaFeeMapping findById(final Long id) {
		return bpaFeeMappingRepository.findOne(id);
	}

	@Transactional
	public List<BpaFeeMapping> update(final List<BpaFeeMapping> bpaFeeMapping) {
		return bpaFeeMappingRepository.save(bpaFeeMapping);
	}

	@Transactional
	public List<BpaFeeMapping> findByFeeCode(final String code) {
		return bpaFeeMappingRepository.findByFeeCode(code);
	}

	@Transactional
	public List<BpaFeeMapping> save(final List<BpaFeeMapping> feeMapList) {
		return bpaFeeMappingRepository.save(feeMapList);
	}
	
	@SuppressWarnings("unchecked")
	public List<BpaFeeMapping> getSanctionFeeForListOfServices(Long serviceType) {
		final Criteria feeCrit = getCurrentSession().createCriteria(BpaFeeMapping.class, "bpaFeeObj")
				.createAlias("bpaFeeObj.serviceType", "servicetypeObj");
		feeCrit.add(Restrictions.eq("servicetypeObj.id", serviceType));
		feeCrit.add(Restrictions.eq("bpaFeeObj.feeSubType", FeeSubType.SANCTION_FEE));
		feeCrit.add(Restrictions.eq("bpaFeeObj.applicationType", FeeApplicationType.PERMIT_APPLICATION));

		return feeCrit.list();
	}
	
	@SuppressWarnings("unchecked")
	public List<BpaFeeMapping> getPermitFeeForListOfServices(Long serviceType, String feeType) {
		final Criteria feeCrit = getCurrentSession().createCriteria(BpaFeeMapping.class, "bpaFeeObj")
				.createAlias("bpaFeeObj.bpaFeeCommon","bpaFeeCommon").createAlias("bpaFeeObj.serviceType", "servicetypeObj");
		feeCrit.add(Restrictions.eq("servicetypeObj.id", serviceType));
		feeCrit.add(Restrictions.eq("bpaFeeObj.feeSubType", FeeSubType.SANCTION_FEE));
		feeCrit.add(Restrictions.ilike("bpaFeeCommon.name", feeType));
		feeCrit.add(Restrictions.eq("bpaFeeObj.applicationType", FeeApplicationType.PERMIT_APPLICATION));

		return feeCrit.list();
	}
	
	
	@SuppressWarnings("unchecked")
	public List<BpaFeeMapping> getFeeForListOfServices(Long serviceType, String feeType) {
		final Criteria feeCrit = getCurrentSession().createCriteria(BpaFeeMapping.class, "bpaFeeObj")
				.createAlias("bpaFeeObj.bpaFeeCommon","bpaFeeCommon").createAlias("bpaFeeObj.serviceType", "servicetypeObj");
		feeCrit.add(Restrictions.eq("servicetypeObj.id", serviceType));
		feeCrit.add(Restrictions.eq("bpaFeeObj.feeSubType", FeeSubType.APPLICATION_FEE));
		feeCrit.add(Restrictions.ilike("bpaFeeCommon.name", feeType));
		feeCrit.add(Restrictions.eq("bpaFeeObj.applicationType", FeeApplicationType.PERMIT_APPLICATION));

		return feeCrit.list();
	}
	
	@SuppressWarnings("unchecked")
	public List<BpaFeeMapping> getOCFeeForListOfServices(Long serviceType, String feeType) {
		final Criteria feeCrit = getCurrentSession().createCriteria(BpaFeeMapping.class, "bpaFeeObj")
				.createAlias("bpaFeeObj.bpaFeeCommon","bpaFeeCommon").createAlias("bpaFeeObj.serviceType", "servicetypeObj");
		feeCrit.add(Restrictions.eq("servicetypeObj.id", serviceType));
		feeCrit.add(Restrictions.eq("bpaFeeObj.feeSubType", FeeSubType.APPLICATION_FEE));
		feeCrit.add(Restrictions.ilike("bpaFeeCommon.name", feeType));
		feeCrit.add(Restrictions.eq("bpaFeeObj.applicationType", FeeApplicationType.OCCUPANCY_CERTIFICATE));

		return feeCrit.list();
	}
	
	@SuppressWarnings("unchecked")
	public List<BpaFeeMapping> getOCSanctionFeeForListOfServices(Long serviceType, String feeType) {
		final Criteria feeCrit = getCurrentSession().createCriteria(BpaFeeMapping.class, "bpaFeeObj")
				.createAlias("bpaFeeObj.bpaFeeCommon","bpaFeeCommon").createAlias("bpaFeeObj.serviceType", "servicetypeObj");
		feeCrit.add(Restrictions.eq("servicetypeObj.id", serviceType));
		feeCrit.add(Restrictions.eq("bpaFeeObj.feeSubType", FeeSubType.SANCTION_FEE));
		feeCrit.add(Restrictions.ilike("bpaFeeCommon.name", feeType));
		feeCrit.add(Restrictions.eq("bpaFeeObj.applicationType", FeeApplicationType.OCCUPANCY_CERTIFICATE));

		return feeCrit.list();
	}
	
	@SuppressWarnings("unchecked")
	public List<BpaFeeMapping> getOCSFeeForListOfServices(Long serviceType) {
		final Criteria feeCrit = getCurrentSession().createCriteria(BpaFeeMapping.class, "bpaFeeObj")
				.createAlias("bpaFeeObj.serviceType", "servicetypeObj");
		feeCrit.add(Restrictions.eq("servicetypeObj.id", serviceType));
		feeCrit.add(Restrictions.eq("bpaFeeObj.feeSubType", FeeSubType.SANCTION_FEE));
		feeCrit.add(Restrictions.eq("bpaFeeObj.applicationType", FeeApplicationType.OCCUPANCY_CERTIFICATE));

		return feeCrit.list();
	}
	
	@SuppressWarnings("unchecked")
	public List<BpaFeeMapping> getAppSubTypeFee(Long serviceType, FeeSubType feeSubType, Long appSubType ) {
		final Criteria feeCrit = getCurrentSession().createCriteria(BpaFeeMapping.class, "bpaFeeObj")
				.createAlias("bpaFeeObj.serviceType", "servicetypeObj");
		feeCrit.add(Restrictions.eq("servicetypeObj.id", serviceType));
		feeCrit.add(Restrictions.eq("bpaFeeObj.feeSubType", feeSubType));
		feeCrit.add(Restrictions.eq("bpaFeeObj.applicationType", FeeApplicationType.PERMIT_APPLICATION));
		feeCrit.add(Restrictions.eq("bpaFeeObj.applicationSubType.id", appSubType));

		return feeCrit.list();
	}
	
	public List<BpaFeeMapping> getPermitFeesByAppType(BpaApplication application, Long serviceTypeId) {
		 List<BpaFeeMapping> sanctionFeeRiskBased;
		 List<BpaFeeMapping> sanctionFeeList = getSanctionFeeForListOfServices(serviceTypeId);
         boolean isRiskBased = applicationSubTypeService.getRiskBasedApplicationTypes().contains(application.getApplicationType());

         if(isRiskBased)
         	sanctionFeeRiskBased = sanctionFeeList.stream()
						.filter(bp -> bp.getBpaFeeCommon().getCode().equals("PF") && bp.getApplicationSubType() == null || ( bp.getApplicationSubType() != null && 
								bp.getApplicationSubType().getName() != application.getApplicationType().getName())).collect(Collectors.toList()); 
         else {
         	sanctionFeeRiskBased = sanctionFeeList.stream()
						.filter(bp -> bp.getApplicationSubType() != null 
				                && bp.getBpaFeeCommon().getCode().equals("PF")).collect(Collectors.toList()); 
         }
         sanctionFeeList.removeAll(sanctionFeeRiskBased);
         return sanctionFeeList;
         
	}
	
	@SuppressWarnings("unchecked")
	public List<BpaFeeMapping> getApplicationFeeByServiceAppType(Long serviceType, String feeType, FeeApplicationType feeAppType) {
		final Criteria feeCrit = getCurrentSession().createCriteria(BpaFeeMapping.class, "bpaFeeObj")
				.createAlias("bpaFeeObj.bpaFeeCommon","bpaFeeCommon").createAlias("bpaFeeObj.serviceType", "servicetypeObj");
		feeCrit.add(Restrictions.eq("servicetypeObj.id", serviceType));
		feeCrit.add(Restrictions.eq("bpaFeeObj.feeSubType", FeeSubType.APPLICATION_FEE));
		feeCrit.add(Restrictions.ilike("bpaFeeCommon.name", feeType));
		feeCrit.add(Restrictions.eq("bpaFeeObj.applicationType", feeAppType));

		return feeCrit.list();
	}

}