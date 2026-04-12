package org.mosqueethonon.service.impl.adhesion;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import org.mosqueethonon.configuration.security.context.SecurityContext;
import org.mosqueethonon.entity.adhesion.AdhesionEntity;
import org.mosqueethonon.entity.mail.MailRequestEntity;
import org.mosqueethonon.enums.DocumentMetadataKey;
import org.mosqueethonon.enums.DocumentRequestType;
import org.mosqueethonon.enums.MailRequestStatut;
import org.mosqueethonon.enums.MailRequestType;
import org.mosqueethonon.enums.ResourceTypeEnum;
import org.mosqueethonon.exception.BadRequestException;
import org.mosqueethonon.exception.ResourceNotFoundException;
import org.mosqueethonon.repository.AdhesionRepository;
import org.mosqueethonon.repository.DocumentRepository;
import org.mosqueethonon.repository.MailRequestRepository;
import org.mosqueethonon.service.adhesion.AdhesionService;
import org.mosqueethonon.service.document.AsyncDocumentService;
import org.mosqueethonon.service.lock.LockService;
import org.mosqueethonon.v1.dto.adhesion.AdhesionDto;
import org.mosqueethonon.v1.dto.adhesion.AdhesionSaveCriteria;
import org.mosqueethonon.v1.enums.StatutInscription;
import org.mosqueethonon.v1.mapper.adhesion.AdhesionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@Service
public class AdhesionServiceImpl implements AdhesionService {

    private AdhesionRepository adhesionRepository;

    private AdhesionMapper adhesionMapper;

    private MailRequestRepository mailRequestRepository;

    private LockService lockService;

    private SecurityContext securityContext;

    private AsyncDocumentService asyncDocumentService;

    private DocumentRepository documentRepository;

    @Override
    @Transactional
    public AdhesionDto createAdhesion(AdhesionDto adhesionDto) {
        // Normalisation des chaines de caractères saisies par l'utilisateur
        adhesionDto.normalize();
        AdhesionEntity adhesionEntity = new AdhesionEntity();
        this.adhesionMapper.updateAdhesion(adhesionDto, adhesionEntity);
        adhesionEntity.setDateInscription(LocalDateTime.now());
        adhesionEntity.setStatut(StatutInscription.PROVISOIRE);
        adhesionEntity = this.adhesionRepository.save(adhesionEntity);
        AdhesionDto resultAdhesionDto = this.adhesionMapper.fromEntityToDto(adhesionEntity);
        this.createMailRequest(resultAdhesionDto);
        this.asyncDocumentService.requestDocumentGeneration(DocumentRequestType.ADHESION, resultAdhesionDto.getId());
        this.documentRepository.findByMetadataKeyAndValue(DocumentMetadataKey.ID_ADHESION, String.valueOf(resultAdhesionDto.getId()))
                .ifPresent(doc -> resultAdhesionDto.setIdDocument(doc.getId()));
        return resultAdhesionDto;
    }

    @Override
    public AdhesionDto findAdhesionById(Long id) {
        AdhesionEntity adhesionEntity = this.adhesionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("L'adhesion recherché n'existe pas ! id = " + id));
        AdhesionDto dto = this.adhesionMapper.fromEntityToDto(adhesionEntity);
        this.documentRepository.findByMetadataKeyAndValue(DocumentMetadataKey.ID_ADHESION, String.valueOf(id))
                .ifPresent(doc -> dto.setIdDocument(doc.getId()));
        return dto;
    }

    @Override
    @Transactional
    public Set<Long> deleteAdhesions(Set<Long> ids) {
        for(Long id : ids) {
            this.lockService.acquireLock(ResourceTypeEnum.ADHESION, id, this.securityContext.getUser());
            this.mailRequestRepository.deleteByTypeAndBusinessIdIn(MailRequestType.ADHESION, Sets.newHashSet(id));
            this.adhesionRepository.deleteById(id);
            this.lockService.releaseLock(ResourceTypeEnum.ADHESION, id, this.securityContext.getUser());
        }
        return ids;
    }

    @Override
    @Transactional
    public Set<Long> patchAdhesions(JsonNode patchesNode) {
        Set<Long> ids = new HashSet<>();
        if (patchesNode.has("adhesions")
                && patchesNode.get("adhesions").elements().hasNext()) {
            patchesNode.get("adhesions").forEach(node -> ids.add(this.patchAdhesion(node)));
        } else {
            throw new BadRequestException("Missing non empty 'adhesions' field to patch adhesions !");
        }
        return ids;
    }

    private Long patchAdhesion(JsonNode patchNode) {
        if (!patchNode.has("id") || !patchNode.get("id").isNumber()) {
            throw new BadRequestException("Missing 'id' field or wrong type (expect Long) to patch adhesion !");
        }
        Long id = patchNode.get("id").asLong();
        this.lockService.acquireLock(ResourceTypeEnum.ADHESION, id, this.securityContext.getUser());
        AdhesionEntity adhesion = this.adhesionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Adhesion not found ! id = " + id));
        if (patchNode.has("statut")) {
            if (patchNode.get("statut").isNull()) {
                adhesion.setStatut(null);
            } else {
                StatutInscription newStatut = StatutInscription.valueOf(patchNode.get("statut").asText());
                if(isStatutChangedToValidated(adhesion.getStatut(), newStatut)) {
                   this.createMailRequest(this.adhesionMapper.fromEntityToDto(adhesion));
                }
                adhesion.setStatut(newStatut);
            }
        }
        this.adhesionRepository.save(adhesion);
        this.lockService.releaseLock(ResourceTypeEnum.ADHESION, id, this.securityContext.getUser());
        return id;
    }

    @Override
    @Transactional
    public AdhesionDto updateAdhesion(Long id, AdhesionDto adhesiondto, AdhesionSaveCriteria saveCriteria) {
        AdhesionEntity adhesion = this.adhesionRepository.findById(id).orElse(null);
        if (adhesion == null) {
            throw new IllegalArgumentException("Inscription not found ! idinsc = " + id);
        }
        boolean isStatutChangedToValidated = this.isStatutChangedToValidated(adhesion.getStatut(), adhesiondto.getStatut());
        this.adhesionMapper.updateAdhesion(adhesiondto, adhesion);
        adhesion = this.adhesionRepository.save(adhesion);
        AdhesionDto resultAdhesiondto = this.adhesionMapper.fromEntityToDto(adhesion);
        if (isStatutChangedToValidated || Boolean.TRUE.equals(saveCriteria.getSendMailConfirmation())) {
            this.createMailRequest(resultAdhesiondto);
        }
        this.asyncDocumentService.requestDocumentGeneration(DocumentRequestType.ADHESION, resultAdhesiondto.getId());
        this.documentRepository.findByMetadataKeyAndValue(DocumentMetadataKey.ID_ADHESION, String.valueOf(resultAdhesiondto.getId()))
                .ifPresent(doc -> resultAdhesiondto.setIdDocument(doc.getId()));
        return resultAdhesiondto;
    }

    private boolean isStatutChangedToValidated(StatutInscription oldStatut, StatutInscription newStatut) {
        return oldStatut == StatutInscription.PROVISOIRE && newStatut == StatutInscription.VALIDEE;
    }

    private void createMailRequest(AdhesionDto adhesion) {
        MailRequestEntity mailRequest = MailRequestEntity.builder()
                .businessId(adhesion.getId())
                .type(MailRequestType.ADHESION)
                .statut(MailRequestStatut.PENDING)
                .build();
        this.mailRequestRepository.save(mailRequest);
    }
}
