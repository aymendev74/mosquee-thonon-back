package org.mosqueethonon.service.impl;

import lombok.AllArgsConstructor;
import org.mosqueethonon.annotations.CodeTarif;
import org.mosqueethonon.entity.TarifEntity;
import org.mosqueethonon.repository.TarifRepository;
import org.mosqueethonon.service.TarifAdminService;
import org.mosqueethonon.v1.dto.InfoTarifDto;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Service
@AllArgsConstructor
public class TarifAdminServiceImpl implements TarifAdminService {

    private TarifRepository tarifRepository;

    @Override
    public InfoTarifDto findInfoTarifByPeriode(Long idPeriode) {
        InfoTarifDto infoTarif = InfoTarifDto.builder().idPeriode(idPeriode).build();
        List<TarifEntity> tarifsByPeriode = this.tarifRepository.findByPeriodeIdAndApplication(idPeriode, "COURS");
        if(!CollectionUtils.isEmpty(tarifsByPeriode)) {
            tarifsByPeriode.stream().forEach(tarif -> {
                setFieldValue(infoTarif, tarif.getCode(), tarif.getMontant());
            });
        }
        return infoTarif;
    }

    private void setFieldValue(InfoTarifDto infoTarif, String codeTarif, BigDecimal montant) {
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
        return codeTarifAnnotation!=null && codeTarifAnnotation.value()!=null &&
                codeTarifAnnotation.value().equals(codeTarif);
    }
}
