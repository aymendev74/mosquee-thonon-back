package org.mosqueethonon.service.impl.referentiel;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.mosqueethonon.annotations.CodeTarif;
import org.mosqueethonon.entity.referentiel.PeriodeEntity;
import org.mosqueethonon.entity.referentiel.TarifEntity;
import org.mosqueethonon.enums.ApplicationTarifEnum;
import org.mosqueethonon.enums.TypeTarifEnum;
import org.mosqueethonon.repository.PeriodeRepository;
import org.mosqueethonon.repository.TarifRepository;
import org.mosqueethonon.service.referentiel.TarifAdminService;
import org.mosqueethonon.v1.dto.referentiel.InfoTarifDto;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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
                infoTarif.setMontant(tarifsByPeriode.get(0).getMontant());
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
        TarifEntity tarif = null;
        if(CollectionUtils.isEmpty(tarifsByPeriode)) {
            tarif = new TarifEntity();
            tarif.setPeriode(periode);
        } else {
            tarif = tarifsByPeriode.get(0);
        }
        tarif.setMontant(infoTarifDto.getMontant());
        tarif.setType(TypeTarifEnum.ADULTE.name());
        this.tarifRepository.save(tarif);
    }

    private void saveInfoTarifEnfant(InfoTarifDto infoTarifDto, PeriodeEntity periode) {
        List<TarifEntity> tarifsByPeriode = this.tarifRepository.findByPeriodeId(infoTarifDto.getIdPeriode());
        Field[] infoTarifDtoFields = infoTarifDto.getClass().getDeclaredFields();
        Map<String, BigDecimal> mapNewTarifByCode = Arrays.stream(infoTarifDtoFields)
                .filter(this::existAnnotationCodeTarif).collect(Collectors.toMap(field -> this.getCodeTarif(field, infoTarifDto), field -> this.getMontant(field, infoTarifDto)));
        // Si pas d'anciens tarif (création), on créé d'abord la base des entités TarifEntity
        if (CollectionUtils.isEmpty(tarifsByPeriode)) {
            if (periode == null) {
                throw new IllegalStateException("La période n'a pas été retrouvée !");
            }
            tarifsByPeriode = Arrays.stream(infoTarifDtoFields).filter(this::existAnnotationCodeTarif)
                    .map(field -> this.createTarif(field, periode))
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
        if (tarifField != null) {
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
        return codeTarifAnnotation != null && codeTarifAnnotation.codeTarif() != null &&
                codeTarifAnnotation.codeTarif().equals(codeTarif);
    }

    private boolean existAnnotationCodeTarif(Field field) {
        CodeTarif codeTarifAnnotation = field.getAnnotation(CodeTarif.class);
        return codeTarifAnnotation != null;
    }
}
