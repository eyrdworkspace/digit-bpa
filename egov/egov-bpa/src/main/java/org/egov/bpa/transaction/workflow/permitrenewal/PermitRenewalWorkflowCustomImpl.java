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
package org.egov.bpa.transaction.workflow.permitrenewal;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.egov.bpa.transaction.entity.BpaStatus;
import org.egov.bpa.transaction.entity.PermitRenewal;
import org.egov.bpa.transaction.entity.SiteDetail;
import org.egov.bpa.transaction.entity.WorkflowBean;
import org.egov.bpa.transaction.service.BpaStatusService;
import org.egov.bpa.transaction.workflow.BpaWorkFlowService;
import org.egov.bpa.utils.BpaConstants;
import org.egov.bpa.utils.BpaUtils;
import org.egov.bpa.utils.PushBpaApplicationToPortalUtil;
import org.egov.eis.entity.Assignment;
import org.egov.eis.service.PositionMasterService;
import org.egov.infra.admin.master.entity.User;
import org.egov.infra.security.utils.SecurityUtils;
import org.egov.infra.workflow.matrix.entity.WorkFlowMatrix;
import org.egov.infra.workflow.service.SimpleWorkflowService;
import org.egov.pims.commons.Position;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author vinoth
 *
 */
public abstract class PermitRenewalWorkflowCustomImpl implements PermitRenewalWorkflowCustom {
    private static final Logger LOG = LoggerFactory.getLogger(PermitRenewalWorkflowCustomImpl.class);

    @Autowired
    private SecurityUtils securityUtils;

    @Autowired
    private PositionMasterService positionMasterService;

    @Autowired
    @Qualifier("workflowService")
    private SimpleWorkflowService<PermitRenewal> permitRenewalWorkflowService;

    @Autowired
    private BpaStatusService bpaStatusService;

    @Autowired
    private BpaWorkFlowService bpaWorkFlowService;

    @Autowired
    private BpaUtils bpaUtils;

    @Autowired
    private PushBpaApplicationToPortalUtil pushBpaApplicationToPortal;

    @Override
    @Transactional
    public void createCommonWorkflowTransition(final PermitRenewal permitRenewal, final WorkflowBean wfBean) {

        if (LOG.isDebugEnabled())
            LOG.debug(" Create Permit Renewal WorkFlow Transition Started  ...");
        final User user = securityUtils.getCurrentUser();
        final DateTime currentDate = new DateTime();
        Position pos = null;

        User ownerUser = null;
        if (wfBean.getApproverPositionId() != null && wfBean.getApproverPositionId() > 0)
            pos = positionMasterService.getPositionById(wfBean.getApproverPositionId());
        if (pos != null)
            ownerUser = bpaWorkFlowService.getAssignmentsByPositionAndDate(pos.getId(), new Date()).get(0).getEmployee();
        WorkFlowMatrix wfmatrix;
        if (null == permitRenewal.getState()) {
            String pendingAction = "Permit renewal application creation pending";
            wfmatrix = permitRenewalWorkflowService.getWfMatrix(permitRenewal.getStateType(), null,
                    null, wfBean.getAdditionalRule(), BpaConstants.WF_NEW_STATE, pendingAction);
            if (wfmatrix != null) {
                if (pos == null) {
                    SiteDetail siteDetail = permitRenewal.getParent().getSiteDetail().get(0);
                    pos = bpaUtils.getUserPositionByZone(wfmatrix.getNextDesignation(),
                            bpaUtils.getBoundaryForWorkflow(siteDetail).getId());
                    List<Assignment> assignments = bpaWorkFlowService.getAssignmentsByPositionAndDate(pos.getId(), new Date());
                    if (!assignments.isEmpty())
                        ownerUser = assignments.get(0).getEmployee();
                }
                permitRenewal.setStatus(getStatusByCurrentMatrxiStatus(wfmatrix));
                permitRenewal.transition().start()
                        .withSenderName(user.getUsername() + BpaConstants.COLON_CONCATE + user.getName())
                        .withComments(wfBean.getApproverComments())
                        .withStateValue(wfmatrix.getNextState()).withDateInfo(new Date()).withOwner(pos).withOwner(ownerUser)
                        .withNextAction(wfmatrix.getNextAction()).withNatureOfTask(BpaConstants.NATURE_OF_WORK_RENEWAL);
            }
        } else if (BpaConstants.WF_APPROVE_BUTTON.equalsIgnoreCase(wfBean.getWorkFlowAction())) {
            wfmatrix = permitRenewalWorkflowService.getWfMatrix(permitRenewal.getStateType(), null,
                    null, wfBean.getAdditionalRule(), permitRenewal.getCurrentState().getValue(), null);
            permitRenewal.setStatus(getStatusByCurrentMatrxiStatus(wfmatrix));
            permitRenewal.transition().progressWithStateCopy()
                    .withSenderName(user.getUsername() + BpaConstants.COLON_CONCATE + user.getName())
                    .withComments(wfBean.getApproverComments())
                    .withStateValue(wfmatrix.getNextState()).withDateInfo(currentDate.toDate())
                    .withOwner(pos).withOwner(ownerUser)
                    .withNextAction(wfmatrix.getNextAction()).withNatureOfTask(BpaConstants.NATURE_OF_WORK_RENEWAL);
        } else if (BpaConstants.WF_REJECT_BUTTON.equalsIgnoreCase(wfBean.getWorkFlowAction())) {
        	wfmatrix = permitRenewalWorkflowService.getWfMatrix(permitRenewal.getStateType(), null,
                    null, wfBean.getAdditionalRule(), BpaConstants.WF_REJECT_STATE, null);
        	permitRenewal.setStatus(getStatusByPassingCode(BpaConstants.WF_REJECT_STATE));
        	permitRenewal.transition().progressWithStateCopy()
                       .withSenderName(user.getUsername() + BpaConstants.COLON_CONCATE + user.getName())
                       .withComments(wfBean.getApproverComments())
                       .withStateValue(BpaConstants.WF_REJECT_STATE).withDateInfo(currentDate.toDate())
                       .withOwner(pos).withOwner(ownerUser)
                       .withNextAction(wfmatrix.getNextAction())
                       .withNatureOfTask(BpaConstants.NATURE_OF_WORK_RENEWAL);
        }else if (BpaConstants.GENERATEREJECTNOTICE.equalsIgnoreCase(wfBean.getWorkFlowAction()) || BpaConstants.WF_CANCELAPPLICATION_BUTTON.equalsIgnoreCase(wfBean.getWorkFlowAction())) {
        	permitRenewal.setStatus(getStatusByPassingCode(BpaConstants.APPLICATION_STATUS_CANCELLED));
        	permitRenewal.transition().end()
                       .withSenderName(user.getUsername() + BpaConstants.COLON_CONCATE + user.getName())
                       .withComments(wfBean.getApproverComments()).withDateInfo(currentDate.toDate())
                       .withNextAction(BpaConstants.WF_END_STATE).withNatureOfTask(BpaConstants.NATURE_OF_WORK_RENEWAL);
        } else {
            wfmatrix = permitRenewalWorkflowService.getWfMatrix(permitRenewal.getStateType(), null,
                    null, wfBean.getAdditionalRule(), permitRenewal.getCurrentState().getValue(),
                    permitRenewal.getState().getNextAction());
            if (wfmatrix != null) {
                BpaStatus status = getStatusByCurrentMatrxiStatus(wfmatrix);
                ownerUser = bpaWorkFlowService.getAssignmentsByPositionAndDate(pos.getId(), new Date()).get(0).getEmployee();
                if (status != null)
                    permitRenewal.setStatus(status);

                if (wfmatrix.getNextAction().contains("END"))
                    permitRenewal.transition().end()
                            .withSenderName(user.getUsername() + BpaConstants.COLON_CONCATE + user.getName())
                            .withComments(wfBean.getApproverComments()).withDateInfo(currentDate.toDate())
                            .withNextAction(wfmatrix.getNextAction()).withNatureOfTask(BpaConstants.NATURE_OF_WORK_RENEWAL);
                else
                    permitRenewal.transition().progressWithStateCopy()
                            .withSenderName(user.getUsername() + BpaConstants.COLON_CONCATE + user.getName())
                            .withComments(wfBean.getApproverComments())
                            .withStateValue(wfmatrix.getNextState()).withDateInfo(currentDate.toDate()).withOwner(pos)
                            .withOwner(ownerUser)
                            .withNextAction(wfmatrix.getNextAction()).withNatureOfTask(BpaConstants.NATURE_OF_WORK_RENEWAL);
            }
        }
        if (LOG.isDebugEnabled())
            LOG.debug("Permit Renewal WorkFlow Transition Completed ");
        pushBpaApplicationToPortal.updatePortalUserinbox(permitRenewal, null);
    }

    private BpaStatus getStatusByCurrentMatrxiStatus(final WorkFlowMatrix wfmatrix) {
        if (wfmatrix != null && StringUtils.isNotBlank(wfmatrix.getNextStatus()))
            return getStatusByPassingCode(wfmatrix.getNextStatus());
        return null;
    }

    private BpaStatus getStatusByPassingCode(final String code) {
        if (StringUtils.isNotBlank(code))
            return bpaStatusService
                    .findByModuleTypeAndCode(BpaConstants.RENEWALSTATUS_MODULETYPE, code);
        return null;
    }

}