package com.neuroguard.assuranceservice.service;

import com.neuroguard.assuranceservice.dto.AssuranceRequestDto;
import com.neuroguard.assuranceservice.dto.AssuranceResponseDto;
import com.neuroguard.assuranceservice.entity.AssuranceStatus;

import java.util.List;

public interface AssuranceService {
    AssuranceResponseDto createAssurance(AssuranceRequestDto request);
    List<AssuranceResponseDto> getAssurancesByPatient(Long patientId);
    List<AssuranceResponseDto> getAllAssurances();
    AssuranceResponseDto getAssuranceById(Long id);
    List<AssuranceResponseDto> getAssurancesByIds(List<Long> ids);
    AssuranceResponseDto updateAssuranceStatus(Long id, AssuranceStatus status);
    AssuranceResponseDto updateAssurance(Long id, AssuranceRequestDto request);
    void deleteAssurance(Long id);
}

