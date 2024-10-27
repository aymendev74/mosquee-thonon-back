package org.mosqueethonon.service.impl.referentiel;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.mosqueethonon.annotations.CodeTarifEnfant;
import org.mosqueethonon.annotations.TarifAdulte;
import org.mosqueethonon.entity.referentiel.PeriodeEntity;
import org.mosqueethonon.entity.referentiel.TarifEntity;
import org.mosqueethonon.enums.TypeTarifEnum;
import org.mosqueethonon.repository.PeriodeRepository;
import org.mosqueethonon.repository.TarifRepository;
import org.mosqueethonon.service.referentiel.TarifAdminService;
import org.mosqueethonon.v1.dto.referentiel.InfoTarifDto;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TarifAdminServiceImpl implements TarifAdminService {

    private TarifRepository tarifRepository;

    private PeriodeRepository periodeRepository;

    @Override
    public InfoTarifDto findInfoTarifByPeriode(Long idPeriode) {
        PeriodeEntity periode = this.periodeRepository.findById(idPeriode).orElse(null);
        if (periode == null) {
            throw new IllegalArgumentException("Periode introuvable ! idperi = " + idPeriode);
        }
        InfoTarifDto infoTarif = InfoTarifDto.builder().idPeriode(idPeriode).build();
        List<TarifEntity> tarifsByPeriode = this.tarifRepository.findByPeriodeId(idPeriode);

        if (!CollectionUtils.isEmpty(tarifsByPeriode)) {
            if ("COURS_ENFANT".equals(periode.getApplication())) {
                tarifsByPeriode.forEach(tarif -> {
                    setFieldValue(infoTarif, tarif.getCode(), tarif.getMontant());
                });
            } else {
                tarifsByPeriode.forEach(tarif -> {
                    setFieldValue(infoTarif, tarif.getType(), tarif.getMontant());
                });
            }
        }
        return infoTarif;
    }

    @Transactional
    @Override
    public InfoTarifDto saveInfoTarif(InfoTarifDto infoTarifDto) {
        // On récupères les anciens tarifs
        PeriodeEntity periode = this.periodeRepository.findById(infoTarifDto.getIdPeriode()).orElse(null);
        if("COURS_ENFANT".equals(periode.getApplication())) {
            this.saveInfoTarifEnfant(infoTarifDto, periode);
        } else {
            this.saveInfoTarifAdulte(infoTarifDto, periode);
        }
        return this.findInfoTarifByPeriode(infoTarifDto.getIdPeriode());
    }

    private void saveInfoTarifAdulte(InfoTarifDto infoTarifDto, PeriodeEntity periode) {
        List<TarifEntity> tarifsByPeriode = this.tarifRepository.findByPeriodeId(infoTarifDto.getIdPeriode());
        Field[] infoTarifDtoFields = infoTarifDto.getClass().getDeclaredFields();
        Map<TypeTarifEnum, BigDecimal> mapNewTarifByType = Arrays.stream(infoTarifDtoFields)
                .filter(field -> existAnnotation(field, TarifAdulte.class)).collect(Collectors.toMap(
                        field -> this.getTypeTarif(field, infoTarifDto), field -> this.getMontant(field, infoTarifDto)));

        // Si pas d'anciens tarif (création), on créé d'abord la base des entités TarifEntity
        if (CollectionUtils.isEmpty(tarifsByPeriode)) {
            if (periode == null) {
                throw new IllegalStateException("La période n'a pas été retrouvée !");
            }
            tarifsByPeriode = Arrays.stream(infoTarifDtoFields).filter(field -> existAnnotation(field, TarifAdulte.class))
                    .map(field -> this.createTarifAdulte(field, periode))
                    .collect(Collectors.toList());
        }

        // Puis on set les montants
        for (Map.Entry<TypeTarifEnum, BigDecimal> entry : mapNewTarifByType.entrySet()) {
            TarifEntity matchingTarif = tarifsByPeriode.stream().filter(tarif -> tarif.getType() == entry.getKey())
                    .findFirst().orElse(null);
            if (matchingTarif != null) {
                matchingTarif.setMontant(entry.getValue());
            }
        }

        // On sauvegarde les nouveaux tarifs
        this.tarifRepository.saveAll(tarifsByPeriode);
    }

    private void saveInfoTarifEnfant(InfoTarifDto infoTarifDto, PeriodeEntity periode) {
        List<TarifEntity> tarifsByPeriode = this.tarifRepository.findByPeriodeId(infoTarifDto.getIdPeriode());
        Field[] infoTarifDtoFields = infoTarifDto.getClass().getDeclaredFields();
        Map<String, BigDecimal> mapNewTarifByCode = Arrays.stream(infoTarifDtoFields)
                .filter(field -> existAnnotation(field, CodeTarifEnfant.class)).collect(Collectors.toMap(
                        field -> this.getCodeTarif(field, infoTarifDto), field -> this.getMontant(field, infoTarifDto)));
        // Si pas d'anciens tarif (création), on créé d'abord la base des entités TarifEntity
        if (CollectionUtils.isEmpty(tarifsByPeriode)) {
            if (periode == null) {
                throw new IllegalStateException("La période n'a pas été retrouvée !");
            }
            tarifsByPeriode = Arrays.stream(infoTarifDtoFields).filter(field -> existAnnotation(field, CodeTarifEnfant.class))
                    .map(field -> this.createTarifEnfant(field, periode))
                    .collect(Collectors.toList());
        }

        // Puis on set les montants
        for (Map.Entry<String, BigDecimal> entry : mapNewTarifByCode.entrySet()) {
            TarifEntity matchingTarif = tarifsByPeriode.stream().filter(tarif -> tarif.getCode().equals(entry.getKey()))
                    .findFirst().orElse(null);
            if (matchingTarif != null) {
                matchingTarif.setMontant(entry.getValue());
            }
        }

        // On sauvegarde les nouveaux tarifs
        this.tarifRepository.saveAll(tarifsByPeriode);
    }

    private TarifEntity createTarifEnfant(Field field, PeriodeEntity periode) {
        CodeTarifEnfant annotationCodeTarif = field.getAnnotation(CodeTarifEnfant.class);
        return TarifEntity.builder().code(annotationCodeTarif.codeTarif()).adherent(annotationCodeTarif.adherent())
                .type(annotationCodeTarif.type()).nbEnfant(annotationCodeTarif.nbEnfant())
                .periode(periode).build();
    }

    private TarifEntity createTarifAdulte(Field field, PeriodeEntity periode) {
        TarifAdulte annotationCodeTarif = field.getAnnotation(TarifAdulte.class);
        return TarifEntity.builder().type(annotationCodeTarif.type()).periode(periode).build();
    }

    private String getCodeTarif(Field field, InfoTarifDto infoTarifDto) {
        return field.getAnnotation(CodeTarifEnfant.class).codeTarif();
    }

    private TypeTarifEnum getTypeTarif(Field field, InfoTarifDto infoTarifDto) {
        return field.getAnnotation(TarifAdulte.class).type();
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
                .filter(field -> hasMatchingAnnotationCodeTarifEnfant(field, codeTarif)).findFirst().orElse(null);
        if (tarifField != null) {
            tarifField.setAccessible(true);
            try {
                tarifField.set(infoTarif, montant);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void setFieldValue(Object infoTarif, TypeTarifEnum typeTarif, BigDecimal montant) {
        Field tarifField = Arrays.stream(infoTarif.getClass().getDeclaredFields())
                .filter(field -> hasMatchingAnnotationTarifAdulte(field, typeTarif)).findFirst().orElse(null);
        if (tarifField != null) {
            tarifField.setAccessible(true);
            try {
                tarifField.set(infoTarif, montant);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean hasMatchingAnnotationCodeTarifEnfant(Field field, String codeTarif) {
        CodeTarifEnfant codeTarifAnnotation = field.getAnnotation(CodeTarifEnfant.class);
        return codeTarifAnnotation != null && codeTarifAnnotation.codeTarif() != null &&
                codeTarifAnnotation.codeTarif().equals(codeTarif);
    }

    private boolean hasMatchingAnnotationTarifAdulte(Field field, TypeTarifEnum typeTarif) {
        TarifAdulte codeTarifAnnotation = field.getAnnotation(TarifAdulte.class);
        return codeTarifAnnotation != null && codeTarifAnnotation.type() != null &&
                codeTarifAnnotation.type().equals(typeTarif);
    }

    private boolean existAnnotation(Field field, Class<? extends Annotation> annotationClass) {
        Annotation annotation = field.getAnnotation(annotationClass);
        return annotation != null;
    }
}
