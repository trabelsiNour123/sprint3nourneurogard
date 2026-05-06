package com.neuroguard.assuranceservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicalHistoryDto {
    private Long id;
    private Long patientId;
    private String diagnosis;
    private String diagnosisDate;
    private String progressionStage;              // MILD, MODERATE, SEVERE
    private String geneticRisk;
    private String familyHistory;
    private String environmentalFactors;
    private String comorbidities;
    private String medicationAllergies;
    private String environmentalAllergies;
    private String foodAllergies;
    private Boolean smoking;
    private Boolean cardiovascularDisease;
    private Boolean diabetes;
    private Boolean depression;
    private Boolean headInjury;
    private Boolean hypertension;
    private Double bmi;
    private Double cholesterolTotal;
    private Integer mmse;                         // Mini-Mental State Exam (0-30)
    private Integer functionalAssessment;         // (0-10)
    private Integer adl;                          // Activities of Daily Living (0-10)
    private Integer alcoholConsumption;           // (0-10)
    private Integer physicalActivity;             // (0-10)
    private Integer dietQuality;                  // (0-10)
    private Integer sleepQuality;                 // (0-10)
    private Boolean memoryComplaints;
    private Boolean behavioralProblems;
}
