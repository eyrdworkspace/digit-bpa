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
package org.egov.bpa.web.controller.transaction.citizen;

import static org.egov.bpa.utils.BpaConstants.APPLICATION_MODULE_TYPE;
import static org.egov.bpa.utils.BpaConstants.AUTH_TO_SUBMIT_PLAN;
import static org.egov.bpa.utils.BpaConstants.BPA_APPLICATION;
import static org.egov.bpa.utils.BpaConstants.CHECKLIST_TYPE;
import static org.egov.bpa.utils.BpaConstants.CHECKLIST_TYPE_NOC;
import static org.egov.bpa.utils.BpaConstants.DCR_CHECKLIST;
import static org.egov.bpa.utils.BpaConstants.DISCLIMER_MESSAGE_ONEDAYPERMIT_ONSAVE;
import static org.egov.bpa.utils.BpaConstants.DISCLIMER_MESSAGE_ONSAVE;
import static org.egov.bpa.utils.BpaConstants.ST_CODE_01;
import static org.egov.bpa.utils.BpaConstants.ST_CODE_02;
import static org.egov.bpa.utils.BpaConstants.ST_CODE_03;
import static org.egov.bpa.utils.BpaConstants.ST_CODE_04;
import static org.egov.bpa.utils.BpaConstants.ST_CODE_05;
import static org.egov.bpa.utils.BpaConstants.ST_CODE_06;
import static org.egov.bpa.utils.BpaConstants.ST_CODE_07;
import static org.egov.bpa.utils.BpaConstants.ST_CODE_08;
import static org.egov.bpa.utils.BpaConstants.ST_CODE_09;
import static org.egov.bpa.utils.BpaConstants.ST_CODE_14;
import static org.egov.bpa.utils.BpaConstants.ST_CODE_15;
import static org.egov.bpa.utils.BpaConstants.WF_LBE_SUBMIT_BUTTON;
import static org.egov.bpa.utils.BpaConstants.WF_NEW_STATE;
import static org.egov.bpa.utils.BpaConstants.WF_SAVE_BUTTON;
import static org.egov.bpa.utils.BpaConstants.WF_SEND_BUTTON;
import static org.egov.infra.persistence.entity.enums.UserType.BUSINESS;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.egov.bpa.master.entity.ApplicationSubType;
import org.egov.bpa.master.entity.ChecklistServiceTypeMapping;
import org.egov.bpa.master.entity.NocConfiguration;
import org.egov.bpa.master.entity.StakeHolder;
import org.egov.bpa.master.entity.enums.StakeHolderStatus;
import org.egov.bpa.master.service.NocConfigurationService;
import org.egov.bpa.transaction.entity.ApplicationFloorDetail;
import org.egov.bpa.transaction.entity.ApplicationStakeHolder;
import org.egov.bpa.transaction.entity.BpaApplication;
import org.egov.bpa.transaction.entity.BuildingDetail;
import org.egov.bpa.transaction.entity.ExistingBuildingDetail;
import org.egov.bpa.transaction.entity.ExistingBuildingFloorDetail;
import org.egov.bpa.transaction.entity.PermitDcrDocument;
import org.egov.bpa.transaction.entity.PermitDocument;
import org.egov.bpa.transaction.entity.PermitNocDocument;
import org.egov.bpa.transaction.entity.common.DcrDocument;
import org.egov.bpa.transaction.entity.common.GeneralDocument;
import org.egov.bpa.transaction.entity.common.NocDocument;
import org.egov.bpa.transaction.entity.enums.ApplicantMode;
import org.egov.bpa.transaction.entity.enums.NocIntegrationInitiationEnum;
import org.egov.bpa.transaction.entity.enums.NocIntegrationTypeEnum;
import org.egov.bpa.transaction.service.ApplicationBpaFeeCalculation;
import org.egov.bpa.transaction.service.BpaDcrService;
import org.egov.bpa.transaction.service.BuildingFloorDetailsService;
import org.egov.bpa.transaction.service.PermitFeeCalculationService;
import org.egov.bpa.transaction.service.PermitNocApplicationService;
import org.egov.bpa.transaction.service.SearchBpaApplicationService;
import org.egov.bpa.transaction.service.collection.GenericBillGeneratorService;
import org.egov.bpa.utils.BpaConstants;
import org.egov.bpa.web.controller.transaction.BpaGenericApplicationController;
import org.egov.commons.entity.Source;
import org.egov.commons.service.SubOccupancyService;
import org.egov.eis.entity.Assignment;
import org.egov.infra.admin.master.entity.AppConfigValues;
import org.egov.infra.admin.master.entity.User;
import org.egov.infra.config.core.ApplicationThreadLocals;
import org.egov.infra.custom.CustomImplProvider;
import org.egov.infra.workflow.matrix.entity.WorkFlowMatrix;
import org.egov.pims.commons.Position;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/application/citizen")
public class CitizenApplicationController extends BpaGenericApplicationController {

    private static final String ONLINE_PAYMENT_ENABLE = "onlinePaymentEnable";

    private static final String WORK_FLOW_ACTION = "workFlowAction";

    private static final String TRUE = "TRUE";

    private static final String CITIZEN_OR_BUSINESS_USER = "citizenOrBusinessUser";

    private static final String IS_CITIZEN = "isCitizen";

    private static final String MSG_PORTAL_FORWARD_REGISTRATION = "msg.portal.forward.registration";

    private static final String MESSAGE = "message";

    private static final String BPAAPPLICATION_CITIZEN = "citizen_suceess";

    private static final String COMMON_ERROR = "common-error";

    @Autowired
    private GenericBillGeneratorService genericBillGeneratorService;
    @Autowired
    private BuildingFloorDetailsService buildingFloorDetailsService;
    @Autowired
    private SearchBpaApplicationService searchBpaApplicationService;
    @Autowired
    private BpaDcrService bpaDcrService;
    @Autowired
    protected SubOccupancyService subOccupancyService;
    @Autowired
    private CustomImplProvider specificNoticeService;
    @Autowired
    private NocConfigurationService nocConfigurationService;
    @Autowired
    private PermitNocApplicationService permitNocService;

    @GetMapping("/newconstruction-form")
    public String showNewApplicationForm(@ModelAttribute final BpaApplication bpaApplication, final Model model,
            final HttpServletRequest request) {
        setCityName(model, request);
        model.addAttribute("currentuser", securityUtils.getCurrentUser().getName());
        return loadNewForm(bpaApplication, model, ST_CODE_01);
    }

    private void setCityName(final Model model, final HttpServletRequest request) {
        if (request.getSession().getAttribute("cityname") != null)
            model.addAttribute("cityName", request.getSession().getAttribute("cityname"));
    }

    private String loadNewForm(final BpaApplication bpaApplication, final Model model, String serviceCode) {
        User user = securityUtils.getCurrentUser();
        StakeHolder stkHldr = stakeHolderService.findById(user.getId());
        if (validateStakeholderIsEligibleSubmitAppln(model, serviceCode, stkHldr))
            return COMMON_ERROR;
        if (user.getType().equals(BUSINESS) && stkHldr.getDemand() != null) {
            List<AppConfigValues> appConfigValueList = appConfigValueService
                    .getConfigValuesByModuleAndKey(APPLICATION_MODULE_TYPE, "BUILDING_LICENSEE_REG_FEE_REQUIRED");
            if ((appConfigValueList.isEmpty() ? "" : appConfigValueList.get(0).getValue()).equalsIgnoreCase("YES")
                    && stkHldr.getStatus() != null
                    && BpaConstants.APPLICATION_STATUS_PENDNING.equalsIgnoreCase(stkHldr.getStatus().name())) {
                return genericBillGeneratorService.generateBillAndRedirectToCollection(stkHldr, model);
            }
        }
        model.addAttribute("stakeHolderType", stkHldr.getStakeHolderType().getName());
        prepareFormData(model);
        bpaApplication.setApplicationDate(new Date());
        prepareCommonModelAttribute(model, bpaApplication.isCitizenAccepted());
        model.addAttribute("mode", "new");
        model.addAttribute("permitApplnFeeRequired", bpaUtils.isApplicationFeeCollectionRequired());
        bpaApplication.setSource(Source.CITIZENPORTAL);
        bpaApplication.setApplicantMode(ApplicantMode.NEW);
        bpaApplication.setServiceType(serviceTypeService.getServiceTypeByCode(serviceCode));
        model.addAttribute("isEDCRIntegrationRequire", bpaDcrService.isEdcrIntegrationRequireByService(serviceCode));
        model.addAttribute("loadingFloorDetailsFromEdcrRequire", true);
        List<ChecklistServiceTypeMapping> list = checklistServiceTypeService
                .findByActiveChecklistAndServiceType(bpaApplication.getServiceType().getDescription(), CHECKLIST_TYPE);
        model.addAttribute("subOccupancyList", subOccupancyService.findAllOrderByOrderNumber());
        List<PermitDocument> appDocList = new ArrayList<>();
        for (ChecklistServiceTypeMapping chklistServiceType : list) {
            PermitDocument permitDoc = new PermitDocument();
            GeneralDocument documentsCommon = new GeneralDocument();
            documentsCommon.setServiceChecklist(chklistServiceType);
            permitDoc.setDocument(documentsCommon);
            appDocList.add(permitDoc);
        }
        List<ChecklistServiceTypeMapping> dcrCheckListDetail = checklistServiceTypeService
                .findByActiveChecklistAndServiceType(bpaApplication.getServiceType().getDescription(), DCR_CHECKLIST);
        if (bpaApplication.getPermitDcrDocuments().isEmpty()) {
            for (ChecklistServiceTypeMapping dcrChkDetails : dcrCheckListDetail) {
                PermitDcrDocument permitDcrDocument = new PermitDcrDocument();
                DcrDocument dcrDocument = new DcrDocument();
                dcrDocument.setServiceChecklist(dcrChkDetails);
                permitDcrDocument.setApplication(bpaApplication);
                permitDcrDocument.setDcrDocument(dcrDocument);
                bpaApplication.getPermitDcrDocuments().add(permitDcrDocument);
            }
        }
        model.addAttribute("isPermitApplFeeReq", "NO");
        model.addAttribute("permitApplFeeCollected", "NO");
        if (bpaUtils.isApplicationFeeCollectionRequired()) {
            model.addAttribute("isPermitApplFeeReq", "YES");
        }
        if (bpaApplication.getDemand() != null
                && bpaApplication.getDemand().getAmtCollected().compareTo(bpaApplication.getAdmissionfeeAmount()) >= 0) {
            model.addAttribute("permitApplFeeCollected", "YES");
        }
        Map<String, String> nocTypeApplMap = new ConcurrentHashMap<>();
        if (bpaApplication.getPermitNocDocuments().isEmpty()) {
            Map<String, String> nocConfigMap = new ConcurrentHashMap<>();
            Map<String, String> nocAutoMap = new ConcurrentHashMap<>();

            List<ChecklistServiceTypeMapping> checklistServicetypeList = checklistServiceTypeService
                    .findByActiveChecklistAndServiceType(bpaApplication.getServiceType().getDescription(),
                            CHECKLIST_TYPE_NOC);
            for (ChecklistServiceTypeMapping serviceChklist : checklistServicetypeList) {
                PermitNocDocument permitNocDocument = new PermitNocDocument();
                NocDocument nocDocument = new NocDocument();
                nocDocument.setServiceChecklist(serviceChklist);
                permitNocDocument.setApplication(bpaApplication);
                permitNocDocument.setNocDocument(nocDocument);
                bpaApplication.getPermitNocDocuments().add(permitNocDocument);
                String code = serviceChklist.getChecklist().getCode();
                NocConfiguration nocConfig = nocConfigurationService.findByDepartmentAndType(code, BpaConstants.PERMIT);
                if (bpaApplication.getApplicationNumber() != null
                        && permitNocService.findByApplicationNumberAndType(bpaApplication.getApplicationNumber(), code) != null)
                    nocTypeApplMap.put(code, "initiated");
                if (nocConfig != null && nocConfig.getApplicationType().trim().equalsIgnoreCase(BpaConstants.PERMIT)
                        && nocConfig.getIntegrationType().equalsIgnoreCase(NocIntegrationTypeEnum.SEMI_AUTO.toString())
                        && nocConfig.getIntegrationInitiation().equalsIgnoreCase(NocIntegrationInitiationEnum.MANUAL.toString()))
                    nocConfigMap.put(nocConfig.getDepartment(), "initiate");
                if (nocConfig != null && nocConfig.getApplicationType().trim().equalsIgnoreCase(BpaConstants.PERMIT)
                        && nocConfig.getIntegrationType().equalsIgnoreCase(NocIntegrationTypeEnum.SEMI_AUTO.toString())
                        && nocConfig.getIntegrationInitiation().equalsIgnoreCase(NocIntegrationInitiationEnum.AUTO.toString()))
                    nocAutoMap.put(nocConfig.getDepartment(), "autoinitiate");

            }
            model.addAttribute("nocConfigMap", nocConfigMap);
            model.addAttribute("nocAutoMap", nocAutoMap);

        }
        model.addAttribute("nocTypeApplMap", nocTypeApplMap);
        model.addAttribute("applicationDocumentList", appDocList);
        getDcrDocumentsUploadMode(model);
        if (!bpaDcrService.isEdcrIntegrationRequireByService(serviceCode)) {
            BuildingDetail bldg = new BuildingDetail();
            bldg.setName("0");
            bldg.setNumber(0);
            bpaApplication.getBuildingDetail().add(bldg);
            ExistingBuildingDetail exstBldg = new ExistingBuildingDetail();
            exstBldg.setName("0");
            exstBldg.setNumber(0);
            bpaApplication.getExistingBuildingDetails().add(exstBldg);
        }
        return "citizenApplication-form";
    }

    private boolean validateStakeholderIsEligibleSubmitAppln(Model model, String serviceCode, StakeHolder stkHldr) {
        if (stkHldr != null && StakeHolderStatus.BLOCKED.equals(stkHldr.getStatus())) {
            model.addAttribute(MESSAGE, messageSource.getMessage("msg.stakeholder.license.blocked",
                    new String[] { ApplicationThreadLocals.getMunicipalityName() }, null));
            return true;
        }
        if (stkHldr != null && stkHldr.getBuildingLicenceExpiryDate() != null && stkHldr.getBuildingLicenceExpiryDate()
                .before(searchBpaApplicationService.resetFromDateTimeStamp(new Date()))) {
            model.addAttribute(MESSAGE, messageSource.getMessage("msg.stakeholder.expiry.reached",
                    new String[] { stkHldr.getName() }, null));
            return true;
        }
        if (stkHldr != null && ("Town Planner - A".equals(stkHldr.getStakeHolderType().getCode())
                || "Town Planner - B".equals(stkHldr.getStakeHolderType().getCode()))
                && !ST_CODE_05.equals(serviceCode)) {
            model.addAttribute(MESSAGE,
                    messageSource.getMessage("msg.invalid.stakeholder", new String[] { stkHldr.getName() }, null));
            return true;
        }
        return false;
    }

    @GetMapping("/demolition-form")
    public String showDemolition(@ModelAttribute final BpaApplication bpaApplication, final Model model,
            final HttpServletRequest request) {
        setCityName(model, request);
        return loadNewForm(bpaApplication, model, ST_CODE_02);
    }

    @GetMapping("/reconstruction-form")
    public String showReconstruction(@ModelAttribute final BpaApplication bpaApplication, final Model model,
            final HttpServletRequest request) {
        setCityName(model, request);
        return loadNewForm(bpaApplication, model, ST_CODE_03);
    }

    @GetMapping("/alteration-form")
    public String showAlteration(@ModelAttribute final BpaApplication bpaApplication, final Model model,
            final HttpServletRequest request) {
        setCityName(model, request);
        return loadNewForm(bpaApplication, model, ST_CODE_04);
    }

    @GetMapping("/subdevland-form")
    public String showSubDevlisionOfLand(@ModelAttribute final BpaApplication bpaApplication, final Model model,
            final HttpServletRequest request) {
        setCityName(model, request);
        return loadNewForm(bpaApplication, model, ST_CODE_05);
    }

    @GetMapping("/addextnew-form")
    public String loadAddOfExtection(@ModelAttribute final BpaApplication bpaApplication, final Model model,
            final HttpServletRequest request) {
        setCityName(model, request);
        return loadNewForm(bpaApplication, model, ST_CODE_06);
    }

    @GetMapping("/changeofoccupancy-form")
    public String showChangeOfOccupancy(@ModelAttribute final BpaApplication bpaApplication, final Model model,
            final HttpServletRequest request) {
        setCityName(model, request);
        return loadNewForm(bpaApplication, model, ST_CODE_07);
    }

    @GetMapping("/permissionhutorshud-form")
    public String loadPermOfHutOrShud(@ModelAttribute final BpaApplication bpaApplication, final Model model,
            final HttpServletRequest request) {
        setCityName(model, request);
        return loadNewForm(bpaApplication, model, ST_CODE_09);
    }

    @GetMapping("/amenity-form")
    public String loadAmenity(@ModelAttribute final BpaApplication bpaApplication, final Model model,
            final HttpServletRequest request) {
        setCityName(model, request);
        return loadNewForm(bpaApplication, model, ST_CODE_08);
    }

    @GetMapping("/towerconstruction-form")
    public String loadTowerConstruction(@ModelAttribute final BpaApplication bpaApplication, final Model model,
            final HttpServletRequest request) {
        setCityName(model, request);
        return loadNewForm(bpaApplication, model, ST_CODE_14);
    }

    @GetMapping("/polestructures-form")
    public String loadPoleStruture(@ModelAttribute final BpaApplication bpaApplication, final Model model,
            final HttpServletRequest request) {
        setCityName(model, request);
        return loadNewForm(bpaApplication, model, ST_CODE_15);
    }

    @PostMapping("/application-create")
    public String createNewConnection(@Valid @ModelAttribute final BpaApplication bpaApplication,
            final HttpServletRequest request, final Model model, final BindingResult errors,
            final RedirectAttributes redirectAttributes) {
        String onedaypermit = BpaConstants.APPLICATION_TYPE_ONEDAYPERMIT.toUpperCase();
        List<ApplicationSubType> riskBasedAppTypes = applicationTypeService.getRiskBasedApplicationTypes();
        if (errors.hasErrors()) {
            buildingFloorDetailsService.buildNewlyAddedFloorDetails(bpaApplication);
            applicationBpaService.buildExistingAndProposedBuildingDetails(bpaApplication);
            prepareCommonModelAttribute(model, bpaApplication.isCitizenAccepted());
            return loadNewForm(bpaApplication, model, bpaApplication.getServiceType().getCode());
        }
        if (bpaApplication.getIsOneDayPermitApplication())
            bpaApplication.setApplicationType(applicationTypeService.findByName(onedaypermit));

       
        Map<String, String> eDcrApplDetails = bpaDcrService
                .checkIsEdcrUsedInBpaApplication(bpaApplication.geteDcrNumber());
        if(!eDcrApplDetails.isEmpty())
        if (eDcrApplDetails.get("isExists").equals("true")) {
            model.addAttribute("eDcrApplExistsMessage", eDcrApplDetails.get(BpaConstants.MESSAGE));
            return loadNewForm(bpaApplication, model, bpaApplication.getServiceType().getCode());
        }
        boolean isEdcrIntegrationRequire = bpaDcrService
                .isEdcrIntegrationRequireByService(bpaApplication.getServiceType().getCode());
        if (isEdcrIntegrationRequire && !eDcrApplDetails.isEmpty() && eDcrApplDetails.get("isExists").equals("true")) {
            buildingFloorDetailsService.buildNewlyAddedFloorDetails(bpaApplication);
            applicationBpaService.buildExistingAndProposedBuildingDetails(bpaApplication);
            prepareCommonModelAttribute(model, bpaApplication.isCitizenAccepted());
            model.addAttribute("eDcrApplExistsMessage", eDcrApplDetails.get(BpaConstants.MESSAGE));
            return loadNewForm(bpaApplication, model, bpaApplication.getServiceType().getCode());
        }

        if (isEdcrIntegrationRequire) {
            bpaApplication.getBuildingDetail().clear();
            for (BuildingDetail bldg : bpaApplication.getBuildingDetailFromEdcr()) {
                List<BuildingDetail> bldgDetails = new ArrayList<>();
                List<ApplicationFloorDetail> floorDetails = new ArrayList<>();
                for (ApplicationFloorDetail floor : bldg.getBuildingFloorDetailsByEdcr())
                    floorDetails.add(floor);
                bldg.setApplicationFloorDetailsForUpdate(floorDetails);
                bldgDetails.add(bldg);
                bpaApplication.getBuildingDetail().addAll(bldgDetails);
            }
            if (!bpaApplication.getExistingBldgDetailFromEdcr().isEmpty()) {
                bpaApplication.getExistingBuildingDetails().clear();
                for (ExistingBuildingDetail existBldg : bpaApplication.getExistingBldgDetailFromEdcr()) {
                    List<ExistingBuildingDetail> existBldgDetails = new ArrayList<>();
                    List<ExistingBuildingFloorDetail> floorDetails = new ArrayList<>();
                    for (ExistingBuildingFloorDetail floor : existBldg.getExistingBldgFloorDetailsFromEdcr())
                        floorDetails.add(floor);
                    existBldg.setExistingBuildingFloorDetailsUpdate(floorDetails);
                    existBldgDetails.add(existBldg);
                    bpaApplication.getExistingBuildingDetails().addAll(existBldgDetails);
                }
            }
        }
    
        applicationBpaService.buildExistingAndProposedBuildingDetails(bpaApplication);
        bpaUtils.saveOrUpdateBoundary(bpaApplication);
        /*
         * if (bpaApplicationValidationService.validateBuildingDetails( bpaApplication, model)) {
         * applicationBpaService.buildExistingAndProposedBuildingDetails( bpaApplication); prepareCommonModelAttribute(model,
         * bpaApplication.isCitizenAccepted()); return loadNewForm(bpaApplication, model,
         * bpaApplication.getServiceType().getCode()); }
         */

        String occupancyName;

        if (bpaApplication.getPermitOccupanciesTemp().size() == 1)
            occupancyName = bpaApplication.getPermitOccupanciesTemp().get(0).getName();
        else if (applicationBpaService.isOccupancyContains(bpaApplication.getPermitOccupanciesTemp(), BpaConstants.INDUSTRIAL))
            occupancyName = BpaConstants.INDUSTRIAL;
        else
            occupancyName = BpaConstants.MIXED_OCCUPANCY;

        if (!isEdcrIntegrationRequire && riskBasedAppTypes.contains(bpaApplication.getApplicationType())) {
            ApplicationSubType applicationType = bpaUtils.getBuildingType(
                    bpaApplication.getSiteDetail().get(0).getExtentinsqmts(),
                    bpaUtils.getBuildingHasHighestHeight(bpaApplication.getBuildingDetail())
                            .getHeightFromGroundWithOutStairRoom(),
                    occupancyName);
            bpaApplication.setApplicationType(applicationType);
        }
        Long approvalPosition = null;
        String workFlowAction = request.getParameter(WORK_FLOW_ACTION);
        Boolean isCitizen = request.getParameter(IS_CITIZEN) != null
                && request.getParameter(IS_CITIZEN).equalsIgnoreCase(TRUE) ? Boolean.TRUE : Boolean.FALSE;
        Boolean citizenOrBusinessUser = request.getParameter(CITIZEN_OR_BUSINESS_USER) != null
                && request.getParameter(CITIZEN_OR_BUSINESS_USER).equalsIgnoreCase(TRUE) ? Boolean.TRUE : Boolean.FALSE;
        Boolean onlinePaymentEnable = request.getParameter(ONLINE_PAYMENT_ENABLE) != null
                && request.getParameter(ONLINE_PAYMENT_ENABLE).equalsIgnoreCase(TRUE) ? Boolean.TRUE : Boolean.FALSE;
        final WorkFlowMatrix wfMatrix = bpaUtils.getWfMatrixByCurrentState(
                bpaApplication.getIsOneDayPermitApplication(), bpaApplication.getStateType(), WF_NEW_STATE,
                bpaApplication.getApplicationType().getName());
        if (wfMatrix != null)
            approvalPosition = bpaUtils.getUserPositionIdByZone(wfMatrix.getNextDesignation(),
                    bpaUtils.getBoundaryForWorkflow(bpaApplication.getSiteDetail().get(0)).getId());
        if (citizenOrBusinessUser && workFlowAction != null && workFlowAction.equals(WF_LBE_SUBMIT_BUTTON)
                && (approvalPosition == 0 || approvalPosition == null)) {
            applicationBpaService.buildExistingAndProposedBuildingDetails(bpaApplication);
            model.addAttribute("noJAORSAMessage", messageSource.getMessage("msg.official.not.exist",
                    new String[] { ApplicationThreadLocals.getMunicipalityName() }, LocaleContextHolder.getLocale()));
            return loadNewForm(bpaApplication, model, bpaApplication.getServiceType().getCode());
        }
        if (citizenOrBusinessUser) {
            if (isCitizen) {
                List<ApplicationStakeHolder> applicationStakeHolders = new ArrayList<>();
                ApplicationStakeHolder applicationStakeHolder = new ApplicationStakeHolder();
                applicationStakeHolder.setApplication(bpaApplication);
                applicationStakeHolder.setStakeHolder(bpaApplication.getStakeHolder().get(0).getStakeHolder());
                applicationStakeHolders.add(applicationStakeHolder);
                bpaApplication.setStakeHolder(applicationStakeHolders);
            } else {
                User user = securityUtils.getCurrentUser();
                StakeHolder stakeHolder = stakeHolderService.findById(user.getId());
                ApplicationStakeHolder applicationStakeHolder = new ApplicationStakeHolder();
                applicationStakeHolder.setApplication(bpaApplication);
                applicationStakeHolder.setStakeHolder(stakeHolder);
                bpaApplication.getStakeHolder().add(applicationStakeHolder);
                Map<Boolean, String> shValidation = bpaApplicationValidationService
                        .checkStakeholderIsValid(bpaApplication);
                if (!shValidation.isEmpty())
                    for (Map.Entry<Boolean, String> keyset : shValidation.entrySet()) {
                        if (!keyset.getKey()) {
                            String message = keyset.getValue();
                            model.addAttribute("invalidStakeholder", message);
                            applicationBpaService.buildExistingAndProposedBuildingDetails(bpaApplication);
                            return loadNewForm(bpaApplication, model, bpaApplication.getServiceType().getCode());
                        }
                    }
            }
        } 
        ApplicationBpaFeeCalculation feeCalculation = (ApplicationBpaFeeCalculation) specificNoticeService
                .find(PermitFeeCalculationService.class, specificNoticeService.getCityDetails());
        if (bpaUtils.isApplicationFeeCollectionRequired())
            bpaApplication.setAdmissionfeeAmount(feeCalculation.setAdmissionFeeAmount(bpaApplication, new ArrayList<>()));
        else
            bpaApplication.setAdmissionfeeAmount(BigDecimal.valueOf(0));

        applicationBpaService.persistOrUpdateApplicationDocument(bpaApplication);
        if (bpaApplication.getOwner().getUser() != null && bpaApplication.getOwner().getUser().getId() == null)
            applicationBpaService.buildOwnerDetails(bpaApplication);

        bpaApplication.setSentToCitizen(workFlowAction != null && workFlowAction.equals(WF_SEND_BUTTON));
        BpaApplication bpaApplicationRes = applicationBpaService.createNewApplication(bpaApplication, workFlowAction);

        if (citizenOrBusinessUser) {
            if (isCitizen)
                bpaUtils.createPortalUserinbox(bpaApplicationRes, Arrays.asList(bpaApplicationRes.getOwner().getUser(),
                        bpaApplicationRes.getStakeHolder().get(0).getStakeHolder()), workFlowAction);
            else {
                if (workFlowAction.equals(WF_SEND_BUTTON) || workFlowAction.equals(WF_LBE_SUBMIT_BUTTON))
                    bpaUtils.createPortalUserinbox(bpaApplicationRes, Arrays.asList(bpaApplicationRes.getOwner().getUser(),
                            securityUtils.getCurrentUser()), workFlowAction);
                else
                    bpaUtils.createPortalUserinbox(bpaApplicationRes, Arrays.asList(securityUtils.getCurrentUser()),
                            workFlowAction);
            }
        }

        // Will redirect to collection, then after collection success will
        // forward to official
        if (workFlowAction != null && workFlowAction.equals(WF_LBE_SUBMIT_BUTTON) && onlinePaymentEnable
                && bpaUtils.checkAnyTaxIsPendingToCollect(bpaApplicationRes.getDemand()))
            return genericBillGeneratorService.generateBillAndRedirectToCollection(bpaApplication, model);
        // When fee collection not require then directly will forward to
        // official
        else if (workFlowAction != null && workFlowAction.equals(WF_LBE_SUBMIT_BUTTON)
                && !bpaUtils.checkAnyTaxIsPendingToCollect(bpaApplication.getDemand())) {
            String remarks = null;
            if (bpaApplication.getAuthorizedToSubmitPlan())
                remarks = AUTH_TO_SUBMIT_PLAN;
            bpaUtils.redirectToBpaWorkFlow(approvalPosition, bpaApplication, WF_NEW_STATE,
                    remarks == null ? bpaApplication.getApprovalComent() : remarks, null, null);
            bpaUtils.sendSmsEmailOnCitizenSubmit(bpaApplication);
            List<Assignment> assignments;
            if (null == approvalPosition)
                assignments = bpaWorkFlowService.getAssignmentsByPositionAndDate(
                        bpaApplication.getCurrentState().getOwnerPosition().getId(), new Date());
            else
                assignments = bpaWorkFlowService.getAssignmentsByPositionAndDate(approvalPosition, new Date());
            Position pos = assignments.get(0).getPosition();
            User wfUser = assignments.get(0).getEmployee();
            String message = messageSource.getMessage(MSG_PORTAL_FORWARD_REGISTRATION,
                    new String[] {
                            wfUser == null ? ""
                                    : wfUser.getUsername().concat("~").concat(getDesinationNameByPosition(pos)),
                            bpaApplication.getApplicationNumber() },
                    LocaleContextHolder.getLocale());
            if (bpaApplication.getIsOneDayPermitApplication()) {
                message = message.concat(DISCLIMER_MESSAGE_ONEDAYPERMIT_ONSAVE);
                getAppointmentMsgForOnedayPermit(bpaApplication, model);
            } else
                message = message.concat(DISCLIMER_MESSAGE_ONSAVE);

            redirectAttributes.addFlashAttribute(MESSAGE, message);
        } else if (workFlowAction != null && workFlowAction.equals(WF_SAVE_BUTTON))
            redirectAttributes.addFlashAttribute(MESSAGE,
                    "Successfully saved with application number " + bpaApplicationRes.getApplicationNumber() + ".");
        else
            redirectAttributes.addFlashAttribute(MESSAGE,
                    "Successfully forwarded application to the citizen with application number "
                            + bpaApplicationRes.getApplicationNumber() + ".");
        
        if (bpaUtils.isCitizenAcceptanceRequired() && !bpaApplicationRes.isCitizenAccepted()
                && workFlowAction != null && workFlowAction.equals(WF_SEND_BUTTON))
            bpaSmsAndEmailService.sendSMSAndEmail(bpaApplicationRes, null, null);
        
        return "redirect:/application/citizen/success/" + bpaApplicationRes.getApplicationNumber();
    }
    
    @GetMapping("/success/{applicationNumber}")
    public String success(@PathVariable final String applicationNumber, final Model model) {
        BpaApplication application = applicationBpaService.findByApplicationNumber(applicationNumber);
        model.addAttribute(BPA_APPLICATION, application);
        return BPAAPPLICATION_CITIZEN;
    }

    @GetMapping("/check-status/{applicationNumber}")
    public String viewApplicationStatus(final Model model, @PathVariable final String applicationNumber,
            final HttpServletRequest request) {
        BpaApplication application = applicationBpaService.findByApplicationNumber(applicationNumber);
        if (application == null)
            model.addAttribute(MESSAGE, messageSource.getMessage("msg.validate.building.plan.approval.no", null, null));
        model.addAttribute("bpaApplication", application);
        return "application-status";
    }
}