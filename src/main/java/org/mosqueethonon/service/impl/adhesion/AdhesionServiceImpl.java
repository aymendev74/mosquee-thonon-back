package org.mosqueethonon.service.impl.adhesion;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import org.mosqueethonon.entity.adhesion.AdhesionEntity;
import org.mosqueethonon.entity.mail.MailingConfirmationEntity;
import org.mosqueethonon.enums.MailingConfirmationStatut;
import org.mosqueethonon.exception.BadRequestException;
import org.mosqueethonon.exception.ResourceNotFoundException;
import org.mosqueethonon.repository.AdhesionRepository;
import org.mosqueethonon.repository.MailingConfirmationRepository;
import org.mosqueethonon.service.adhesion.AdhesionService;
import org.mosqueethonon.v1.dto.adhesion.AdhesionDto;
import org.mosqueethonon.v1.enums.StatutInscription;
import org.mosqueethonon.v1.mapper.adhesion.AdhesionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@AllArgsConstructor
@Service
public class AdhesionServiceImpl implements AdhesionService {

    private AdhesionRepository adhesionRepository;

    private AdhesionMapper adhesionMapper;

    private MailingConfirmationRepository mailingConfirmationRepository;

    @Override
    @Transactional
    public AdhesionDto createAdhesion(AdhesionDto adhesionDto) {
        // Normalisation des chaines de caractères saisies par l'utilisateur
        adhesionDto.normalize();
        AdhesionEntity adhesionEntity = new AdhesionEntity();
        this.adhesionMapper.mapDtoToEntity(adhesionDto, adhesionEntity);
        adhesionEntity.setDateInscription(LocalDateTime.now());
        adhesionEntity.setStatut(StatutInscription.PROVISOIRE);
        adhesionEntity = this.adhesionRepository.save(adhesionEntity);
        adhesionDto = this.adhesionMapper.fromEntityToDto(adhesionEntity);
        this.mailingConfirmationRepository.save(MailingConfirmationEntity.builder().idAdhesion(adhesionEntity.getId())
                .statut(MailingConfirmationStatut.PENDING).build());
        return adhesionDto;
    }

    @Override
    public AdhesionDto findAdhesionById(Long id) {
        Optional<AdhesionEntity> optAdhesionEntity = this.adhesionRepository.findById(id);
        return optAdhesionEntity.map(adhesion -> this.adhesionMapper.fromEntityToDto(adhesion)).orElse(null);
    }

    @Override
    @Transactional
    public Set<Long> deleteAdhesions(Set<Long> ids) {
        this.adhesionRepository.deleteAllById(ids);
        return ids;
    }

    @Override
    @Transactional
    public Set<Long> patchAdhesions(JsonNode patchesNode) {
        Set<Long> ids = new HashSet<>();
        if(patchesNode.has("adhesions")
        && patchesNode.get("adhesions").elements().hasNext()) {
            patchesNode.get("adhesions").forEach(node -> ids.add(this.patchAdhesion(node)));
        }
        return ids;
    }

    private Long patchAdhesion(JsonNode patchNode) {
        if(!patchNode.has("id") || !patchNode.get("id").isNumber()) {
            throw new BadRequestException("Missing 'id' field or wrong type (expect Long) to patch adhesion !");
        }
        Long id = patchNode.get("id").asLong();
        AdhesionEntity adhesion = this.adhesionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Adhesion not found ! id = " + id));
        if (patchNode.has("statut")) {
            if(patchNode.get("statut").isNull()) {
                adhesion.setStatut(null);
            } else {
                adhesion.setStatut(StatutInscription.valueOf(patchNode.get("statut").asText()));
            }
        }
        this.adhesionRepository.save(adhesion);
        return id;
    }

    @Override
    @Transactional
    public AdhesionDto updateAdhesion(Long id, AdhesionDto adhesiondto) {
        AdhesionEntity adhesion = this.adhesionRepository.findById(id).orElse(null);
        if (adhesion == null) {
            throw new IllegalArgumentException("Inscription not found ! idinsc = " + id);
        }
        this.adhesionMapper.mapDtoToEntity(adhesiondto, adhesion);
        adhesion = this.adhesionRepository.save(adhesion);
        return this.adhesionMapper.fromEntityToDto(adhesion);
    }
}
