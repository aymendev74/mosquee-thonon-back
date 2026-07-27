package org.mosqueethonon.referentiel.service.impl;

import lombok.AllArgsConstructor;
import org.mosqueethonon.common.exception.ResourceNotFoundException;
import org.mosqueethonon.referentiel.repository.TraductionRepository;
import org.mosqueethonon.referentiel.service.TraductionService;
import org.mosqueethonon.referentiel.v1.dto.TraductionDto;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TraductionServiceImpl implements TraductionService {

    private TraductionRepository traductionRepository;

    @Override
    public TraductionDto findTraductionByCleAndValeur(String cle, String valeur) {
        return this.traductionRepository.findByCleAndValeur(cle, valeur).map(tradEntity -> TraductionDto.builder()
                .code(tradEntity.getValeur()).fr(tradEntity.getFr()).build())
                .orElseThrow(() -> new ResourceNotFoundException("La traduction n'a pas été trouvée avec les critères suivants " +
                        "cle : " + cle + " et valeur : " + valeur));
    }

}
