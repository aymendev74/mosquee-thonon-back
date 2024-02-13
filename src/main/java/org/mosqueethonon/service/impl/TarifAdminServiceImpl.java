package org.mosqueethonon.service.impl;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.mosqueethonon.annotations.CodeTarif;
import org.mosqueethonon.entity.PeriodeEntity;
import org.mosqueethonon.entity.TarifEntity;
import org.mosqueethonon.repository.PeriodeRepository;
import org.mosqueethonon.repository.TarifRepository;
import org.mosqueethonon.service.TarifAdminService;
import org.mosqueethonon.v1.dto.InfoTarifDto;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TarifAdminServiceImpl implements TarifAdminService {

    private TarifRepository tarifRepository;

    private PeriodeRepository periodeRepository;

    @Override
    public InfoTarifDto findInfoTarifByPeriode(Long idPeriode) {
        InfoTarifDto infoTarif = InfoTarifDto.builder().idPeriode(idPeriode).build();
        List<TarifEntity> tarifsByPeriode = this.tarifRepository.findByPeriodeIdAndPeriodeApplication(idPeriode, "COURS");
        if(!CollectionUtils.isEmpty(tarifsByPeriode)) {
            tarifsByPeriode.stream().forEach(tarif -> {
                setFieldValue(infoTarif, tarif.getCode(), tarif.getMontant());
            });
        }
        return infoTarif;
    }

    @Transactional
    @Override
    public InfoTarifDto saveInfoTarif(InfoTarifDto infoTarifDto) {
        // On récupères les anciens tarifs
        List<TarifEntity> tarifsByPeriode = this.tarifRepository.findByPeriodeIdAndPeriodeApplication(infoTarifDto.getIdPeriode(), "COURS");
        Field[] infoTarifDtoFields = infoTarifDto.getClass().getDeclaredFields();
        Map<String, BigDecimal> mapNewTarifByCode = Arrays.stream(infoTarifDtoFields)
                .filter(this::existAnnotationCodeTarif).collect(Collectors.toMap(field -> this.getCodeTarif(field, infoTarifDto), field -> this.getMontant(field, infoTarifDto)));
        // Si pas d'anciens tarif (création), on créé d'abord la base des entités TarifEntity
        if(CollectionUtils.isEmpty(tarifsByPeriode)) {
            Optional<PeriodeEntity> optPeriode = this.periodeRepository.findById(infoTarifDto.getIdPeriode());
            if(!optPeriode.isPresent()) {
                throw new IllegalStateException("La période n'a pas été retrouvée !");
            }
            tarifsByPeriode = Arrays.stream(infoTarifDtoFields).filter(this::existAnnotationCodeTarif)
                    .map(field -> this.createTarif(field, optPeriode.get()))
                    .collect(Collectors.toList());
        }

        // Puis on set les montants
        for(Map.Entry<String, BigDecimal> entry : mapNewTarifByCode.entrySet()) {
            TarifEntity matchingTarif = tarifsByPeriode.stream().filter(tarif -> tarif.getCode().equals(entry.getKey()))
                    .findFirst().orElse(null);
            if(matchingTarif != null) {
                matchingTarif.setMontant(entry.getValue());
            }
        }

        // On sauvegarde les nouveaux tarifs
        this.tarifRepository.saveAll(tarifsByPeriode);

        return this.findInfoTarifByPeriode(infoTarifDto.getIdPeriode());
    }

    private TarifEntity createTarif(Field field, PeriodeEntity periode) {
        CodeTarif annotationCodeTarif = field.getAnnotation(CodeTarif.class);
        return TarifEntity.builder().code(annotationCodeTarif.codeTarif()).adherent(annotationCodeTarif.adherent())
                .type(annotationCodeTarif.type()).nbEnfant(annotationCodeTarif.nbEnfant())
                .periode(periode).build();
    }

    private String getCodeTarif(Field field, InfoTarifDto infoTarifDto) {
        return field.getAnnotation(CodeTarif.class).codeTarif();
    }

    private BigDecimal getMontant(Field field, InfoTarifDto infoTarifDto) {
        try {
            field.setAccessible(true);
            return (BigDecimal) field.get(infoTarifDto);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void setFieldValue(Object infoTarif, String codeTarif, BigDecimal montant) {
        Field tarifField = Arrays.stream(infoTarif.getClass().getDeclaredFields())
                .filter(field -> hasMatchingAnnotationCode(field, codeTarif)).findFirst().orElse(null);
        if (tarifField!=null) {
            tarifField.setAccessible(true);
            try {
                tarifField.set(infoTarif, montant);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean hasMatchingAnnotationCode(Field field, String codeTarif) {
        CodeTarif codeTarifAnnotation = field.getAnnotation(CodeTarif.class);
        return codeTarifAnnotation!=null && codeTarifAnnotation.codeTarif()!=null &&
                codeTarifAnnotation.codeTarif().equals(codeTarif);
    }

    private boolean existAnnotationCodeTarif(Field field) {
        CodeTarif codeTarifAnnotation = field.getAnnotation(CodeTarif.class);
        return codeTarifAnnotation!=null;
    }
}
