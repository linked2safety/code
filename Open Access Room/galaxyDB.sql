-- Database: galaxy_prod

drop table Association cascade;
drop table DataMiningAlgorithm cascade;
drop table DimensionalityReductionAlgorithm cascade;
drop table QualityControl cascade;
drop table SafetyAlertNotificationSubscription cascade;
drop table StatisticalAlgorithm cascade;
drop table SubjectCount cascade;
drop table DatasetUsed cascade;
drop table WorkflowUsed cascade;
drop table AssociationRule cascade; 
drop table Users cascade;
drop table AdverseEvents cascade;
drop table PeriodicBatchProcess cascade;
drop table VariableCombinationsToAnalyse cascade;
drop table SelfReportedAssociation cascade;
drop table SelfReportedAssociationRule cascade;
drop table LogFile cascade;
drop table filteringaction cascade;

create table Users (
  userID serial primary key,
  Name varchar(128),
  Occupation varchar(128),
  Organization varchar(128),
  Email_address varchar(128),
  Role varchar(128),
  Subscription_date date,
  Working_area varchar(128),	
  Country varchar(128),
  is_expert boolean default False
);


create table PeriodicBatchProcess(
  PeriodicBP_ID serial primary key,
  days smallint,
  months smallint,
  years smallint,
  previous_date_of_execution date,
  next_date_of_execution date
);

create table WorkflowUsed (
  workflowID serial primary key,  
  datasetID serial,
  workflow varchar(1024),
  userID serial references Users(userID),
  PeriodicBP_ID serial references PeriodicBatchProcess(PeriodicBP_ID)
);

create table FilteringAction(
  actionID serial primary key,
  userID serial references Users(userID),
  association varchar(512),
  associationRule varchar(1024),
  reasoning varchar(1024),
  filtering_date timestamp default now()
);

create table DatasetUsed (
  datasetID serial primary key,
  SPARQL_query text,
  retrieval_date timestamp default now()
);

create table LogFile(
  LogFile_ID serial primary key,
  DatasetFilenames varchar(1024),
  remainingdatasetfilenames varchar(1024),
  fileContent text,
  userID serial references Users(userID)
);

create table SelfReportedAssociation (
  sr_associationID serial primary key,
  Association varchar(512),
  odds_ratio double precision,
  p_value double precision,
  LD real,
  reference varchar(512),
  notes varchar(1024),
  userID serial references Users(userID),
  visible bool default True
);


create table SafetyAlertNotificationSubscription (
  ProfileID serial primary key,
  timestamp_of_alert timestamp,  
  Profile varchar(1024),
  AdverseEvents varchar(1024),
  HealthExpertID serial,
  Subscription varchar(3),
  userID serial references Users(userID)
);

create table SelfReportedAssociationRule (
  RuleID serial primary key,
  LeftHandSide varchar(512),
  RightHandSide varchar(512),
  Support real,
  Confidence real,
  Lift real,
  RelativeReportRatio real,
  reference varchar(512),
  notes varchar(1024),
  userID serial references Users(userID),
  visible bool default True
);

create table SubjectCount (
  Count_of_subjects integer,
  Other_information varchar(1024),
  workflowID serial references WorkflowUsed(workflowID),
  primary key (workflowID)
);

create table DimensionalityReductionAlgorithm (
  DR_algorithm_id serial primary key,  
  Algorithm_name varchar(128),
  AlgorithmParamsSetting varchar(1024),
  workflowID serial references WorkflowUsed(workflowID)
);

create table DataMiningAlgorithm (
  Algorithm_name varchar(128),
  AlgorithmParamsSetting varchar(1024), 
  workflowID serial references WorkflowUsed(workflowID),
  primary key(workflowID)
);

create table QualityControl (
  QC_algorithm_id serial primary key,
  Algorithm_name varchar(128),
  AlgorithmParamsSetting varchar(1024),
  workflowID serial references WorkflowUsed(workflowID)
);

create table StatisticalAlgorithm (
  Algorithm_name varchar(128),
  AlgorithmParamsSetting varchar(1024),
  workflowID serial references WorkflowUsed(workflowID),
  primary key(workflowID)
);

create table Association (
  AssociationID serial primary key,
  Association varchar(512),
  workflowID serial references StatisticalAlgorithm(workflowID),
  odds_ratio double precision,
  p_value double precision,
  LD real,
  visible bool default True
);

create table AssociationRule (
  RuleID serial primary key,
  LeftHandSide varchar(512),
  RightHandSide varchar(512),
  Support real,
  Confidence real,
  Lift real,
  visible bool default True,
  workflowID serial references DataMiningAlgorithm(workflowID)
);

create table AdverseEvents (
  AdverseEvent varchar(128) primary key
);

create table VariableCombinationsToAnalyse (
  VariableCombination varchar(512) primary key,
  date_analysed date
);

alter table VariableCombinationsToAnalyse owner to galaxy;
alter table AdverseEvents owner to galaxy;
alter table AssociationRule owner to galaxy;
alter table Association owner to galaxy;
alter table StatisticalAlgorithm owner to galaxy;
alter table QualityControl owner to galaxy;
alter table DataMiningAlgorithm owner to galaxy;
alter table DimensionalityReductionAlgorithm owner to galaxy;
alter table SubjectCount owner to galaxy;
alter table SelfReportedAssociationRule owner to galaxy;
alter table SafetyAlertNotificationSubscription owner to galaxy;
alter table LogFile owner to galaxy;
alter table SelfReportedAssociation owner to galaxy;
alter table DatasetUsed  owner to galaxy;
alter table FilteringAction owner to galaxy;
alter table WorkflowUsed owner to galaxy;
alter table PeriodicBatchProcess owner to galaxy;
alter table Users owner to galaxy;
