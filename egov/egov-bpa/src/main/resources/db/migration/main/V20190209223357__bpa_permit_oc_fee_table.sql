
create table EGBPA_OCCUPANCY_FEE
	(
	  id bigint NOT NULL,
	  OC bigint,
	  APPLICATIONFEE bigint,
	  createdby bigint NOT NULL,
      createddate timestamp without time zone NOT NULL,
 	  lastModifiedDate timestamp without time zone,
 	  lastModifiedBy bigint,
	  version numeric NOT NULL,
	  CONSTRAINT PK_OCCUPANCY_FEE_ID PRIMARY KEY (ID),
	  CONSTRAINT FK_EGBPA_OCCUPANCY_FEE_OC FOREIGN KEY (OC) REFERENCES EGBPA_OCCUPANCY_CERTIFICATE(ID),
	  CONSTRAINT FK_EGBPA_OCCUPANCY_FEE_APPLFEE FOREIGN KEY (APPLICATIONFEE) REFERENCES EGBPA_APPLICATION_FEE(ID),
	  CONSTRAINT FK_EGBPA_OCCUPANCY_FEE_MDFDBY FOREIGN KEY (lastModifiedBy) REFERENCES EG_USER (ID),
      CONSTRAINT FK_EGBPA_OCCUPANCY_FEE_CRTBY FOREIGN KEY (createdBy)REFERENCES EG_USER (ID)
   );
 create sequence SEQ_EGBPA_OCCUPANCY_FEE;


create table EGBPA_PERMIT_FEE
	(
	  id bigint NOT NULL,
	  APPLICATION bigint,
	  APPLICATIONFEE bigint,
	  createdby bigint NOT NULL,
      createddate timestamp without time zone NOT NULL,
 	  lastModifiedDate timestamp without time zone,
 	  lastModifiedBy bigint,
	  version numeric NOT NULL,
	  CONSTRAINT PK_PERMIT_FEE_ID PRIMARY KEY (ID),
	  CONSTRAINT FK_EGBPA_PERMIT_FEE_APPL FOREIGN KEY (APPLICATION) REFERENCES EGBPA_APPLICATION(ID),
	  CONSTRAINT FK_EGBPA_PERMIT_FEE_APPLFEE FOREIGN KEY (APPLICATIONFEE) REFERENCES EGBPA_APPLICATION_FEE(ID),
	  CONSTRAINT FK_EGBPA_PERMIT_FEE_MDFDBY FOREIGN KEY (lastModifiedBy) REFERENCES EG_USER (ID),
      CONSTRAINT FK_EGBPA_PERMIT_FEE_CRTBY FOREIGN KEY (createdBy)REFERENCES EG_USER (ID)
        );
 create sequence SEQ_EGBPA_PERMIT_FEE;
 
 