package org.mosqueethonon.service.impl.referentiel;

import lombok.AllArgsConstructor;
import org.mosqueethonon.exception.ResourceNotFoundException;
import org.mosqueethonon.repository.TraductionRepository;
import org.mosqueethonon.service.referentiel.TraductionService;
import org.mosqueethonon.v1.dto.referentiel.TraductionDto;
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
